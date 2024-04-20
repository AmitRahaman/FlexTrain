package com.example.flextrain.messaging;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flextrain.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private MessageClickListener clickListener;
    private FirebaseUser currentUser; // Add FirebaseUser for the current user
    private DatabaseReference usersRef; // Add DatabaseReference for users

    // Constructor to initialize the list of messages and the click listener
    public MessageAdapter(List<Message> messages, MessageClickListener clickListener) {
        this.messages = messages;
        this.clickListener = clickListener;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser(); // Get current user
        this.usersRef = FirebaseDatabase.getInstance("https://flextrain-6df5f-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users"); // Reference to users node
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single message item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // Bind the message data to the ViewHolder at the specified position
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        // Return the total number of messages in the list
        return messages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView senderNameText;
        private TextView messageText;
        private ImageView messageImageView;
        private ImageView deleteButton;

        public MessageViewHolder(@NonNull View itemView, final MessageClickListener clickListener) {
            super(itemView);

            senderNameText = itemView.findViewById(R.id.sender_name_text_view);
            messageText = itemView.findViewById(R.id.message_content_text_view);
            messageImageView = itemView.findViewById(R.id.message_image_view);
            deleteButton = itemView.findViewById(R.id.delete_button);

            // Set OnClickListener for the delete button
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog(clickListener);
                }
            });
        }

        // Method to show delete confirmation dialog
        private void showDeleteConfirmationDialog(final MessageClickListener clickListener) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setMessage("Are you sure you want to delete this message?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                clickListener.onDeleteClick(position);
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

        // Method to bind message data to the views
        public void bind(Message message) {
            // Display sender's name instead of UID
            String senderId = message.getSenderId();
            usersRef.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("name").getValue(String.class);
                        if (name != null) {
                            senderNameText.setText(name);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to read sender's name: " + databaseError.getMessage());
                }
            });

            messageText.setText(message.getText());

            // Check if the message has an image URL
            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                // Load the image using Picasso
                Picasso.get().load(message.getImageUrl()).into(messageImageView);
                // Set visibility of the image view to VISIBLE
                messageImageView.setVisibility(View.VISIBLE);
            } else {
                // If no image URL provided, hide the image view
                messageImageView.setVisibility(View.GONE);
            }

            // Check if the message belongs to the current user for deletion permission
            if (currentUser != null && message.getSenderId().equals(currentUser.getUid())) {
                deleteButton.setVisibility(View.VISIBLE); // Show delete button for messages sent by the current user
            } else {
                deleteButton.setVisibility(View.GONE); // Hide delete button for other users' messages
            }
        }
    }
}



