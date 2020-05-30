package eu.clarin.sru.fcs.aggregator;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import de.mpg.aai.shhaa.model.AuthAttribute;
import de.mpg.aai.shhaa.model.AuthAttributes;
import de.mpg.aai.shhaa.model.AuthPrincipal;

/**
 * Wrapper class to hold the userPrincipal and a displayName
 *
 */
public class UserCredentials {

    private final Principal userPrincipal;

    public UserCredentials(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    public Principal getPrincipal() {
        return userPrincipal;
    }

    public String getPrincipalName() {
        return userPrincipal.getName();
    }

    public String getPrincipalNameMD5Hex() {
        return getPrincipalNameMD5Hex(userPrincipal.getName());
    }

    public static String getPrincipalNameMD5Hex(String name){
    return DigestUtils.md5Hex(name);
    }

    public String getDisplayName() {
        String result = null;
        if (userPrincipal instanceof AuthPrincipal) {
            List<String> displayNamesAttributes = new ArrayList<String>();
            displayNamesAttributes.add("displayName");
            displayNamesAttributes.add("commonName");

            AuthPrincipal authPrincipal = (AuthPrincipal) userPrincipal;
            for (String key : displayNamesAttributes) {
                result = getValue(authPrincipal, key);
                if (result != null) {
                    break;
                }
            }
        }
        if (result == null) {
            result = getPrincipalName();
        }
        return result;
    }

    private String getValue(AuthPrincipal authPrincipal, String key) {
        String result = null;
        AuthAttributes attributes = authPrincipal.getAttribues();
        if (attributes != null) {
            AuthAttribute<String> authAttribute = (AuthAttribute<String>) attributes.get(key);
            if (authAttribute != null) {
                result = authAttribute.getValue();
            }
        }
        return result;
    }

    @Override
    public String toString() {
    return getPrincipal().toString();
    }
}
