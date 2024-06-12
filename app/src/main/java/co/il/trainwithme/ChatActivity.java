package co.il.trainwithme;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private String chatId, friendUsername, myUsername;
    private FirebaseFirestore fstore;
    private FirebaseAuth fAuth;
    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private CollectionReference messagesRef;
    private ImageButton backButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        TextView chatTitle = findViewById(R.id.chatTitle);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        EditText editTextMessage = findViewById(R.id.editTextMessage);
        ImageButton buttonSend = findViewById(R.id.buttonSend);
        backButton = findViewById(R.id.backButton);

        chatId = getIntent().getStringExtra("chatId");
        friendUsername = getIntent().getStringExtra("friendUsername");

        chatTitle.setText("Chat with " + friendUsername);

        fstore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        messagesRef = fstore.collection("chats").document(chatId).collection("messages");

        // Fetch the current user's username
        fetchMyUsername(new UsernameCallback() {
            @Override
            public void onUsernameFetched(String username) {
                myUsername = username;
            }
        });

        // Setup RecyclerView
        chatAdapter = new ChatAdapter();
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        // Load existing messages and listen for new ones
        loadChatMessages();

        buttonSend.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                fetchMyUsername(new UsernameCallback() {
                    @Override
                    public void onUsernameFetched(String username) {
                        myUsername = username;
                        sendMessage(message);
                        editTextMessage.setText("");
                    }
                });
            }
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, PersonalArea.class);
            startActivity(intent);
        });
    }

    private void fetchMyUsername(UsernameCallback callback) {
        String userId = fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = fstore.collection("users").document(userId);
        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                String username = documentSnapshot.getString("username");
                callback.onUsernameFetched(username);
            }
        });
    }

    private void loadChatMessages() {
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            chatAdapter.addMessage(dc.getDocument().toObject(ChatMessage.class));
                            recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                        }
                    }
                });
    }

    private void sendMessage(String message) {
        String userId = fAuth.getCurrentUser().getUid();
        Map<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("senderId", userId);
        chatMessage.put("senderUsername", myUsername);
        chatMessage.put("message", message);
        chatMessage.put("timestamp", Timestamp.now());

        messagesRef.add(chatMessage);
    }

    // Internal ChatMessage class
    private static class ChatMessage {
        private String senderId;
        private String senderUsername;
        private String message;
        private Timestamp timestamp;

        public ChatMessage() {
            // Required for Firebase
        }

        public ChatMessage(String senderId, String senderUsername, String message, Timestamp timestamp) {
            this.senderId = senderId;
            this.senderUsername = senderUsername;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getSenderId() {
            return senderId;
        }

        public String getSenderUsername() {
            return senderUsername;
        }

        public String getMessage() {
            return message;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private List<ChatMessage> chatMessages = new ArrayList<>();
        private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatMessage chatMessage = chatMessages.get(position);
            holder.messageTextView.setText(chatMessage.getSenderUsername() + ": " + chatMessage.getMessage());
            holder.dateTextView.setText(dateFormat.format(chatMessage.getTimestamp().toDate()));
            holder.timeTextView.setText(timeFormat.format(chatMessage.getTimestamp().toDate()));

            if (chatMessage.getSenderId().equals(fAuth.getCurrentUser().getUid())) {
                holder.messageContainer.setBackgroundResource(R.drawable.message_background_sent);
            } else {
                holder.messageContainer.setBackgroundResource(R.drawable.message_background_received);
            }
        }

        @Override
        public int getItemCount() {
            return chatMessages.size();
        }

        public void addMessage(ChatMessage chatMessage) {
            chatMessages.add(chatMessage);
            notifyItemInserted(chatMessages.size() - 1);
        }

        class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView messageTextView;
            TextView dateTextView;
            TextView timeTextView;
            LinearLayout messageContainer;

            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                messageTextView = itemView.findViewById(R.id.messageTextView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
                messageContainer = itemView.findViewById(R.id.messageContainer);
            }
        }
    }


    // Callback interface for fetching username
    private interface UsernameCallback {
        void onUsernameFetched(String username);
    }
}
