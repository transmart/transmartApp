import org.transmart.searchapp.AccessLog


public class AccessLogService {

    static transactional = true

    def adminLog(user, message) {
        def al = new AccessLog(username: user.username, event: "ADMIN", eventmessage: message, accesstime: new Date())
        al.save();
    }

}
