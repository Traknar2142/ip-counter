package org.example.factory;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ChunkWriterFactory {
    public DataOutputStream getWriter(File chunk) throws IOException {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(chunk));
        return new DataOutputStream(outputStream);
    }
}
