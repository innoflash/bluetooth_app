package net.faithgen.bluetooth.interfaces;

public interface ChatDialogListener {
    /**
     * Called when dialog is opened
     */
    void onOpened();

    /**
     * Called when dialog is closed
     */
    void onClose();

    void onMessageSent(String message);

  //  String onMessageSent();
}
