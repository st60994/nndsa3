package cz.upce.nndsa3.data;


public class Index {
    private Integer beginningID;
    private Long position;

    public Index(Integer beginningID, Long position) {
        this.beginningID = beginningID;
        this.position = position;
    }

    public Integer getBeginningID() {
        return beginningID;
    }

    public void setBeginningID(Integer beginningID) {
        this.beginningID = beginningID;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }
}
