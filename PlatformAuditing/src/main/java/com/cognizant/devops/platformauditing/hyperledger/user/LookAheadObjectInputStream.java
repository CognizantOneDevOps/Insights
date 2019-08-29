package com.cognizant.devops.platformauditing.hyperledger.user;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
 
public class LookAheadObjectInputStream extends ObjectInputStream {
 
    public LookAheadObjectInputStream(InputStream inputStream)
            throws IOException {
        super(inputStream);
    }
 
    /**
     * Only deserialize instances of our expected UserUtil class
     */
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
            ClassNotFoundException {
        if (!desc.getName().equals(UserUtil.class.getName())) {
            throw new InvalidClassException(
                    "Unauthorized deserialization attempt",
                    desc.getName());
        }
        return super.resolveClass(desc);
    }
}