package net.faithgen.bluetooth.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.faithgen.bluetooth.R;
import net.faithgen.bluetooth.interfaces.ChatDialogListener;
import net.innoflash.iosview.DialogFullScreen;
import net.innoflash.iosview.DialogToolbar;

public class ChatDialog extends DialogFullScreen {
    public static final String DIALOG_TAG = "dialog_tag";
    private View view;
    private DialogToolbar dialogToolbar;
    private EditText message;
    private ImageView sendButton;
    private TextView responseText;
    private ChatDialogListener chatDialogListener;
    private String receivedMessage;

    public ChatDialog() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_chat, container, false);
        dialogToolbar = view.findViewById(R.id.dialog_toolbar);
        message = view.findViewById(R.id.message);
        sendButton = view.findViewById(R.id.sendButton);
        responseText = view.findViewById(R.id.response);

        dialogToolbar.setDialogFragment(this);
        sendButton.setOnClickListener(view1 -> sendMessage());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (chatDialogListener != null)
            chatDialogListener.onOpened();

    }

    private void sendMessage() {
        if (chatDialogListener != null){
            chatDialogListener.onMessageSent(message.getText().toString() + "\n");
            responseText.setText(responseText.getText().toString() + ">>> " + message.getText().toString() + "\n");
            message.setText("");
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (chatDialogListener != null)
            chatDialogListener.onClose();
    }

    public void setReceivedMessage(String receivedMessage) {
        this.receivedMessage = receivedMessage;
        responseText.setText(responseText.getText().toString() + "<<< " + receivedMessage);
    }

    public void setChatDialogListener(ChatDialogListener chatDialogListener) {
        this.chatDialogListener = chatDialogListener;
    }
}
