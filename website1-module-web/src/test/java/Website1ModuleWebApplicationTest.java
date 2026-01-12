

import org.junit.jupiter.api.Test;

import java.security.MessageDigest;

public class Website1ModuleWebApplicationTest {

    @Test
    public void testGetUserEncryptPassword() {
        String email = "admin@example.com";
        String password = "123456";

        String pas = getUserEncryptPassword(email, password);
        System.out.println(pas);
        System.out.println(getUserEncryptPassword("coder_li@example.com", "123456"));
        System.out.println(getUserEncryptPassword("test1@example.com", "123456"));

    }

    // 盐值数组
    private static final String[] salts = {"sun","moon","star","sky","cloud","fog","rain","wind","rainbow"};

    public static String getUserSalt(String account) {
        int hashCode = account.hashCode() + 159;
        int mod = Math.abs(hashCode % 9);
        return salts[mod];
    }

    public static String getUserEncryptPassword(String email, String password) {
        String pwdAndSalt = password + getUserSalt(email);
        return MD5Encode(pwdAndSalt, "utf8");
    }

    public static String MD5Encode(String str, String charset) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes(charset));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    result.append("0");
                }
                result.append(hex);
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
