package tn.esprit.models;

public class Discussion {
    private int id;
    private int senderId;
    private int receiverId;
    private String senderNomPrenom;
    private String receiverNomPrenom;

    public Discussion(int id, int senderId, int receiverId) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public Discussion(int id, int senderId, int receiverId, String senderNomPrenom, String receiverNomPrenom) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderNomPrenom = senderNomPrenom;
        this.receiverNomPrenom = receiverNomPrenom;
    }

    @Override
    public String toString() {
        return senderNomPrenom + " ➜ " + receiverNomPrenom;
    }

    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }

    public String getSenderNomPrenom() { return senderNomPrenom; }
    public String getReceiverNomPrenom() { return receiverNomPrenom; }

    public void setSenderNomPrenom(String senderNomPrenom) { this.senderNomPrenom = senderNomPrenom; }
    public void setReceiverNomPrenom(String receiverNomPrenom) { this.receiverNomPrenom = receiverNomPrenom; }
}
