package com.sankuai.octo.msgp.domain.auth;

public class PatriotAuthenticationResponse {
    private Boolean success;
    private String message;
    private PatriotAuthenticationRequest data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PatriotAuthenticationRequest getData() {
        return data;
    }

    public void setData(PatriotAuthenticationRequest data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PatriotAuthenticationResponse{");
        sb.append("success=").append(success);
        sb.append(", message='").append(message).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
