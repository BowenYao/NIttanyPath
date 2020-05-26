import java.util.Date;

public class NittanyCourse {
    private String name,id,description;
    private int section,sectionLimit,teachingTeamId;
    private Date lateDropDeadline;
    public NittanyCourse(String name, String id, String description,int section, int sectionLimit, int teachingTeamId, Date lateDropDeadline){
        this.name = name;
        this.id = id;
        this.description = description;
        this.section = section;
        this.sectionLimit = sectionLimit;
        this.teachingTeamId = teachingTeamId;
        this.lateDropDeadline = lateDropDeadline;
    }

    public String getName(){ return name;}
    public String getId(){return id;}
    public String getDescription(){return description;}
    public int getSection(){return section;}
    public int getSectionLimit(){return sectionLimit;}
    public int getTeachingTeamId(){return teachingTeamId;}
    public Date getLateDropDeadline(){return lateDropDeadline;}


}
