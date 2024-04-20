package com.example.flextrain.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flextrain.R;
import com.example.flextrain.workoutdatabase.Workout;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private static final String TAG = "WorkoutAdapter";

    private List<Workout> workouts = new ArrayList<>();

    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.bind(workout);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView descriptionTextView;
        private TextView categoryTextView;
        private WebView youtubeWebView;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            categoryTextView = itemView.findViewById(R.id.category_text_view);
            youtubeWebView = itemView.findViewById(R.id.youtube_web_view);

            // Configure WebView settings
            youtubeWebView.getSettings().setJavaScriptEnabled(true);
        }

        public void bind(Workout workout) {
            nameTextView.setText(workout.getName());
            descriptionTextView.setText(workout.getDescription());
            categoryTextView.setText(workout.getCategory() + " - " + workout.getExpertiseLevel());

            // Load YouTube video URL
            String youtubeUrl = workout.getYoutubeUrl();
            if (youtubeUrl != null && !youtubeUrl.isEmpty()) {
                Log.d(TAG, "YouTube URL: " + youtubeUrl);
                String videoId = extractVideoId(youtubeUrl);
                if (videoId != null && !videoId.isEmpty()) {
                    String embeddedUrl = "https://www.youtube.com/embed/" + videoId;
                    String iframe = "<iframe width=\"100%\" height=\"auto\" src=\"" + embeddedUrl + "\" frameborder=\"0\" allowfullscreen></iframe>";
                    Log.d(TAG, "Embedded URL: " + embeddedUrl);
                    Log.d(TAG, "Iframe: " + iframe);
                    youtubeWebView.loadData(iframe, "text/html", "utf-8");
                } else {
                    Log.e(TAG, "Failed to extract video ID from YouTube URL: " + youtubeUrl);
                }
            } else {
                Log.e(TAG, "YouTube URL is empty or null");
                // Show error message if no YouTube URL is provided
                youtubeWebView.loadData("An error occurred", "text/html", "utf-8");
            }
        }

        // Extracts the video ID from the YouTube URL
        private String extractVideoId(String youtubeUrl) {
            String videoId = null;
            if (youtubeUrl != null && youtubeUrl.trim().length() > 0) {
                String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed\\/%3Fv%3D|^youtu.be%2F)([\\w\\-_]*)";
                Pattern compiledPattern = Pattern.compile(pattern);
                Matcher matcher = compiledPattern.matcher(youtubeUrl);
                if (matcher.find()) {
                    videoId = matcher.group();
                }
            }
            return videoId;
        }
    }

    private void openYoutubeVideo(Context context, String youtubeUrl) {
        // Open the YouTube video using an Intent
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl));
        intent.putExtra("force_fullscreen", true); // Force fullscreen mode
        context.startActivity(intent);
    }
}
