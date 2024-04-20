package com.example.flextrain.ui.notifications;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flextrain.R;
import com.example.flextrain.messaging.Message;
import com.example.flextrain.messaging.MessageAdapter;
import com.example.flextrain.messaging.MessageClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    private DatabaseReference messagesRef;
    private FirebaseUser currentUser;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firebase user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        messageAdapter = new MessageAdapter(messageList, new MessageClickListener() {
            @Override
            public void onMessageClick(Message message) {

            }

            @Override
            public void onDeleteClick(int position) {
                if (currentUser != null) {
                    // Get the message at the clicked position
                    Message message = messageList.get(position);
                    // Check if the message belongs to the current user
                    if (message.getSenderId().equals(currentUser.getUid())) {
                        // Delete the message from Firebase
                        String messageId = message.getMessageId();
                        messagesRef.child(messageId).removeValue()
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Message deleted successfully from Firebase."))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete message from Firebase: " + e.getMessage()));
                    }
                }
            }
        });
        recyclerView.setAdapter(messageAdapter);

        // Initialize Firebase database reference
        messagesRef = FirebaseDatabase.getInstance("https://flextrain-6df5f-default-rtdb.europe-west1.firebasedatabase.app/").getReference("messages");

        //  listener for retrieving messages from Firebase
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messageList.add(message);
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read messages from Firebase: " + databaseError.getMessage());
            }
        });

        // UI for sending messages
        EditText messageInput = view.findViewById(R.id.message_input);
        Button sendButton = view.findViewById(R.id.send_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = messageInput.getText().toString();
                if (!messageContent.isEmpty()) {
                    sendMessage(messageContent);
                    messageInput.setText(""); // Clear the input field after sending
                }
            }
        });

        return view;
    }

    // Method to send messages to Firebase
    private void sendMessage(String messageContent) {
        if (currentUser == null) {
            Log.e(TAG, "User is not authenticated.");
            return;
        }
        String userId = currentUser.getUid();
        String imageUrl = "";
        long timestamp = System.currentTimeMillis();

        // Generate unique message ID
        String messageId = messagesRef.push().getKey();

        Message message = new Message(messageContent, imageUrl, userId, messageId, timestamp);
        // Set the unique ID for the message
        message.setMessageId(messageId);

        // Push the message to Firebase
        messagesRef.child(messageId).setValue(message)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Message sent successfully to Firebase."))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send message to Firebase: " + e.getMessage()));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
