package cn.ztuo.bitrade.exception;

public class GeneralException extends Exception {
    private String errorCode;
    private String provinceCode;

    public GeneralException(String message) {
        super(message);
    }

    public GeneralException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public GeneralException(String errorCode, Throwable cause) {
        this(errorCode, errorCode, cause);
    }

    public GeneralException(String errorCode, String message, String provinceCode) {
        super(message);
        this.errorCode = errorCode;
        this.provinceCode = provinceCode;
    }

    public GeneralException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public GeneralException(String errorCode, String message, String provinceCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.provinceCode = provinceCode;
    }

    public String getProvinceCode() {
        return this.provinceCode != null && !"".equalsIgnoreCase(this.provinceCode) ? this.provinceCode : "000";
    }

    public String getErrorCode() {
        return this.errorCode != null && !"".equalsIgnoreCase(this.errorCode) ? this.errorCode : "000000";
    }

    public String getMessage() {
        String message = super.getMessage();
        return message;
    }
}
