package com.example.timsanbong.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timsanbong.R;
import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.data.model.MatchPost;
import com.example.timsanbong.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyMatchPostsActivity extends AppCompatActivity {

    private RecyclerView rvMyMatchPosts;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private MatchAdapter adapter;
    private MatchViewModel matchViewModel;

    private final ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadMyPosts();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_my_match_posts);

        rvMyMatchPosts = findViewById(R.id.rvMyMatchPosts);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvMyMatchPosts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MatchAdapter(new ArrayList<>(), new MatchAdapter.OnMatchActionListener() {
            @Override
            public void onAccept(MatchPost match, int position) {
                // Not applicable for my own posts in this view, 
                // but we could use it for "Chốt kèo" if we had a single request
            }

            @Override
            public void onChat(MatchPost match) {
                // Not applicable
            }

            @Override
            public void onCardClick(MatchPost match) {
                Intent intent = new Intent(MyMatchPostsActivity.this, MatchDetailActivity.class);
                intent.putExtra(Constants.EXTRA_MATCH_POST, match);
                startForResult.launch(intent);
            }
        });
        rvMyMatchPosts.setAdapter(adapter);

        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        loadMyPosts();
    }

    private void loadMyPosts() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getService(this).getMyMatchPosts().enqueue(new Callback<List<MatchPost>>() {
            @Override
            public void onResponse(Call<List<MatchPost>> call, Response<List<MatchPost>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<MatchPost> posts = response.body();
                    adapter.updateMatches(posts);
                    tvEmpty.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(MyMatchPostsActivity.this, "Không thể tải bài đăng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MatchPost>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MyMatchPostsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
