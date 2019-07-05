package controller;

import model.Conversation;
import model.Email;

import java.util.ArrayList;
import java.util.List;

class ListArranger {
    static List<Conversation> arrangeByUnread(List<Conversation> list) {
        List<Conversation> ans = new ArrayList<>(list.size());
        for (Conversation c : list) {
            boolean allRead = c.getMessages().stream().allMatch(Email::isRead);
            if (allRead)
                ans.add(c);
        }
        for (Conversation c : list) {
            if (!ans.contains(c))
                ans.add(c);
        }
        return ans;
    }
}
