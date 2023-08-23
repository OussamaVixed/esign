package esign.service;

import esign.model.Groups;
import esign.repository.GroupsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupsService {

    private final GroupsRepository groupsRepository;
    
    @Autowired
    public GroupsService(GroupsRepository groupsRepository) {
        this.groupsRepository = groupsRepository;
    }

    public void createGroup(String owner, String groupname, List<String> members) {
        Groups group = new Groups();
        group.setOwner(owner);
        group.setGroupname(groupname);
        group.setMembers(members);

        groupsRepository.save(group);
    }

    public List<String> getGroupMembersByUsernameAndGroupName(String username, String groupname) {
        Groups group = groupsRepository.findByGroupnameAndOwner(groupname, username);
        if (group != null) {
            return group.getMembers();
        }
        return null; // Return null or handle error appropriately
    }
    
    public List<String> getGroupNamesByUsername(String username) {
        List<Groups> groups = groupsRepository.findByOwner(username);
        if (groups != null) {
            return groups.stream().map(Groups::getGroupname).collect(Collectors.toList());
        }
        return null; // Return null or handle error appropriately
    }
}
