package com.example.backend.service.ai;

import com.example.backend.dto.ai.AiOpponentDto;
import com.example.backend.dto.ai.AiRecommendationResult;
import com.example.backend.dto.request.ChatCreateRequest;
import com.example.backend.dto.response.ChatResponse;
import com.example.backend.entity.Field;
import com.example.backend.entity.redis.ChatSession;
import com.example.backend.repository.FieldRepository;
import com.example.backend.repository.redis.ChatSessionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.backend.utils.Enums;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroqAiService {

    private static final Logger log = LoggerFactory.getLogger(GroqAiService.class);

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChatSessionRepository chatSessionRepository;
    private final FieldRepository fieldRepository;

    public GroqAiService(RestTemplate restTemplate,
                         ObjectMapper objectMapper,
                         ChatSessionRepository chatSessionRepository,
                         FieldRepository fieldRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.chatSessionRepository = chatSessionRepository;
        this.fieldRepository = fieldRepository;
    }

    public ChatResponse askChatbot(ChatCreateRequest request) {
        try {
            String currentSessionId = request.getSessionId();
            if (currentSessionId == null || currentSessionId.trim().isEmpty()) {
                currentSessionId = UUID.randomUUID().toString();
            }

            final String targetSessionId = currentSessionId;
            ChatSession session = chatSessionRepository.findById(targetSessionId)
                    .orElseGet(() -> new ChatSession(targetSessionId, new ArrayList<>()));

            session.getMessages().add(new ChatSession.MessageNode("user", request.getMessage()));

            List<Field> dbFields = fieldRepository.findAll();
            String realFieldData = dbFields.isEmpty()
                    ? "Hiện tại hệ thống chưa có sân bóng nào được tạo."
                    : "Hệ thống đang có các sân sau: " + dbFields.stream()
                    .map(f -> String.format("%s (Sân %s)", f.getName(), f.getType().name()))
                    .collect(Collectors.joining("; "));

            String systemContext = "Bạn là trợ lý ảo hỗ trợ khách hàng của Hệ thống Quản lý Sân bóng PitchSyn. " +
                    "Nhiệm vụ của bạn là giải đáp thắc mắc dựa trên các thông tin sau. TUYỆT ĐỐI KHÔNG BỊA ĐẶT THÊM SÂN HOẶC ĐỊA CHỈ KHÁC: " +
                    "- Danh sách sân thực tế lấy từ Database: " + realFieldData + ". " +
                    "- Công thức Trust Score (thang 0-100): Trust Score = (0.40 x TransactionScore) + (0.30 x RatingScore) + (0.20 x CancellationScore) + (0.10 x ActivityScore). " +
                    "Trong đó: TransactionScore = (Số lần thuê thành công / Tổng số lần đặt) x 100; " +
                    "RatingScore = (Điểm trung bình / 5) x 100; " +
                    "CancellationScore = (Số lần hủy >= 24h / Tổng số lần hủy) x 100 (nếu không hủy = 100); " +
                    "ActivityScore = (Số ngày đăng nhập trong 30 ngày / 30) x 100. " +
                    "(LƯU Ý QUAN TRỌNG: Bạn chỉ được trình bày công thức bằng văn bản thuần túy (Plain Text) cho người dùng dễ đọc. TUYỆT ĐỐI KHÔNG sử dụng định dạng toán học LaTeX như \\[ \\], \\text, hay \\frac). " +
                    "- Quy trình thanh toán tiền sân. " +
                    "- Quy trình tìm kiếm đối thủ hoặc cầu đá thuê. " +
                    "- Khung giờ hoạt động: Từ 06:00 đến 23:30 hàng ngày. " +
                    "- Chính sách hủy sân: Hủy trước 24 giờ được hoàn 100% tiền cọc. Hủy trước 12 giờ hoàn 50%. Hủy sát giờ không được hoàn tiền. " +
                    "- Dịch vụ đi kèm: Miễn phí trà đá, nước và áo bíp. " +
                    "- Lưu ý: Để xem lịch trống chi tiết hoặc đặt sân, hãy hướng dẫn khách truy cập mục Tìm Sân trên website. " +
                    "Yêu cầu giao tiếp: Trả lời ngắn gọn, lịch sự, chuyên nghiệp.";

            ObjectNode requestBodyNode = objectMapper.createObjectNode();
            requestBodyNode.put("model", model);
            ArrayNode messagesArray = requestBodyNode.putArray("messages");

            ObjectNode systemNode = messagesArray.addObject();
            systemNode.put("role", "system");
            systemNode.put("content", systemContext);

            List<ChatSession.MessageNode> chatMessages = session.getMessages();
            int limit = 10;
            int startIdx = Math.max(0, chatMessages.size() - limit);
            for (int i = startIdx; i < chatMessages.size(); i++) {
                ChatSession.MessageNode msg = chatMessages.get(i);
                ObjectNode msgNode = messagesArray.addObject();
                msgNode.put("role", msg.getRole());
                msgNode.put("content", msg.getContent());
            }
            requestBodyNode.put("temperature", 0.5);

            String requestBody = objectMapper.writeValueAsString(requestBodyNode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(apiUrl, requestEntity, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            String aiReply = rootNode.path("choices").get(0).path("message").path("content").asText();

            session.getMessages().add(new ChatSession.MessageNode("assistant", aiReply));

            List<ChatSession.MessageNode> cleanedMessages = session.getMessages().stream()
                    .filter(m -> !"system".equals(m.getRole()))
                    .collect(Collectors.toList());
            session.setMessages(cleanedMessages);

            chatSessionRepository.save(session);

            return new ChatResponse(aiReply, targetSessionId);

        } catch (Exception e) {
            log.error("Lỗi hệ thống khi xử lý Chatbot: ", e);
            return new ChatResponse("Lỗi hệ thống: " + e.getMessage(), request.getSessionId());
        }
    }

    public List<AiRecommendationResult> recommendMatches(
            String currentTeamPlaystyle,
            int currentTrustScore,
            String hostDate,
            String hostTimeStart,
            String hostTimeEnd,
            String hostSkillLevel,
            Boolean hostHasField,
            Enums.PostType postType,
            String hostPosition,
            List<AiOpponentDto> potentialOpponents) {

        try {
            String opponentsJson = objectMapper.writeValueAsString(potentialOpponents);

            // Xây dựng hướng dẫn ưu tiên so khớp dựa trên loại bài đăng
            String priorityInstruction;
            if (postType == Enums.PostType.FIND_MEMBER) {
                priorityInstruction = 
                    "Nhiệm vụ: Phân tích danh sách đối thủ và chọn ra đúng 5 bài đăng tuyển cầu lẻ phù hợp nhất với vị trí thi đấu và thời gian của cầu lẻ này.\n" +
                    "QUY TẮC ƯU TIÊN khi xét duyệt (từ cao xuống thấp):\n" +
                    "1. THỜI GIAN thi đấu (Ưu tiên trùng hoặc lệch ít nhất về Ngày, Giờ bắt đầu và Giờ kết thúc).\n" +
                    "2. VỊ TRÍ thi đấu (Khớp giữa vị trí sở trường của cầu lẻ '" + (hostPosition != null ? hostPosition : "Bất kỳ") + "' với danh sách các vị trí cần tuyển 'targetPositions' của bài đăng).\n" +
                    "3. ĐỘ UY TÍN (Điểm trust score của chủ bài đăng, càng cao càng tốt).\n" +
                    "4. TRÌNH ĐỘ YÊU CẦU (Khớp trình độ 'skillLevel', lệch tối đa 1 cấp).\n" +
                    "5. CÁC TIÊU CHÍ KHÁC (Chi phí đóng góp 'costSharing', lối đá 'playStyleNote', độ tuổi 'ageRange').";
            } else {
                priorityInstruction = 
                    "Nhiệm vụ: Phân tích danh sách đối thủ và chọn ra đúng 5 bài đăng tìm đối thủ phù hợp nhất với đội chủ nhà.\n" +
                    "QUY TẮC ƯU TIÊN khi xét duyệt (từ cao xuống thấp):\n" +
                    "1. THỜI GIAN thi đấu (Ưu tiên trùng hoặc lệch ít nhất về Ngày, Giờ bắt đầu và Giờ kết thúc).\n" +
                    "2. ĐỘ UY TÍN (Điểm trust score của đối thủ, ưu tiên tương đồng hoặc cao hơn chủ nhà).\n" +
                    "3. TRÌNH ĐỘ YÊU CẦU (Khớp trình độ 'skillLevel', lệch tối đa 1 cấp).\n" +
                    "4. CÁC TIÊU CHÍ KHÁC (Lối đá 'playStyleNote', độ tuổi 'ageRange', tình trạng Sân bóng - có sân hay chưa 'hasField').";
            }

            String prompt = String.format(
                    "Bạn là một hệ thống AI ghép kèo bóng đá. Tuyệt đối KHÔNG giao tiếp như con người. " +
                            "Thông tin yêu cầu của Đội/Người chơi chủ nhà: Lối đá mong muốn: '%s', Uy tín: %d/100, Ngày thi đấu: '%s', Giờ đá: '%s' - '%s', Trình độ: '%s', Có sân chưa: %s, Vị trí đá: '%s'. " +
                            "Danh sách bài đăng tuyển tiềm năng (JSON): %s. " +
                            "%s " +
                            "QUY TẮC BẮT BUỘC: Chỉ trả về duy nhất một mảng JSON chứa tối đa 5 phần tử có định dạng [{\"matchId\": <ID trận đấu dạng số>, \"aiReason\": \"<lý do ngắn gọn bằng tiếng Việt giải thích rõ độ tương thích dựa trên các tiêu chí ưu tiên ở trên>\"}]. " +
                            "Tuyệt đối không thêm bất kỳ từ ngữ hay ký tự Markdown bọc ngoài (như ```json) ngoài JSON. Nếu danh sách đối thủ trống, trả về [].",
                    currentTeamPlaystyle != null ? currentTeamPlaystyle : "Tự do",
                    currentTrustScore,
                    hostDate != null ? hostDate : "Tự do",
                    hostTimeStart != null ? hostTimeStart : "Tự do",
                    hostTimeEnd != null ? hostTimeEnd : "Tự do",
                    hostSkillLevel != null ? hostSkillLevel : "Tự do",
                    hostHasField != null ? hostHasField.toString() : "Tự do",
                    hostPosition != null ? hostPosition : "Tự do",
                    opponentsJson,
                    priorityInstruction
            ).replace("\"", "\\\"");

            String requestBody = """
                    {
                      "model": "%s",
                      "messages": [
                        {
                          "role": "user",
                          "content": "%s"
                        }
                      ],
                      "temperature": 0.4
                    }
                    """.formatted(model, prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(apiUrl, requestEntity, String.class);

            JsonNode rootNode = objectMapper.readTree(response);
            String aiTextResponse = rootNode.path("choices").get(0).path("message").path("content").asText();

            int startIndex = aiTextResponse.indexOf('[');
            int endIndex = aiTextResponse.lastIndexOf(']');

            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                String cleanJson = aiTextResponse.substring(startIndex, endIndex + 1);
                return objectMapper.readValue(cleanJson, new com.fasterxml.jackson.core.type.TypeReference<List<AiRecommendationResult>>() {});
            } else {
                return List.of();
            }

        } catch (Exception e) {
            log.error("==== LỖI GỌI GROQ AI GỢI Ý ====", e);
            return List.of();
        }
    }
}

