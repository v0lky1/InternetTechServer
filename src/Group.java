import java.util.ArrayList;

public class Group {

    private String owner;
    private String groupName;
    private ArrayList<String> members;

    public Group(String owner, String groupName){
        this.owner = owner;
        this.groupName = groupName;
        this.members = new ArrayList<>();
        members.add(owner);
    }

    public String getOwner() {
        return owner;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean addMember(String username) {
        for (String member: members){
            if (member.equals(username)){
                return false;
            }
        }
        members.add(username);
        return true;
    }

    public void removeMember(String username) {
        members.remove(username);
    }

    public boolean isMember(String username){
        for (String member: members){
            if (member.equals(username)){
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getMembers() {
        return members;
    }
}
