package de.zell;

import sun.nio.ch.DirectBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * Hello world!
 *
 */
public class App 
{
    protected static final String CHECKSUM_ALGORITHM = "SHA1";

    public static void main( String[] args) throws Exception
    {

        if (args.length != 1)
        {
            System.out.println("Need file to read!");
            return;
        }

        System.out.println(args[0]);

        final File dataFile = new File(args[0]);

        final MessageDigest messageDigest = MessageDigest.getInstance(CHECKSUM_ALGORITHM);

        final FileInputStream fileInputStream = new FileInputStream(dataFile);
        final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        final DigestInputStream digestInputStream = new DigestInputStream(bufferedInputStream, messageDigest);

        System.out.println(digestInputStream.toString());

        byte buffer[] = new byte[256];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);

        digestInputStream.read(buffer, 0, 4);
        int countOfParts = byteBuffer.getInt(0);
        System.out.println(countOfParts);

        for (int i = 0; i < countOfParts; i++)
        {
            digestInputStream.read(buffer, 0, 4);
            int length = byteBuffer.getInt(0);
            System.out.println(length);

            digestInputStream.read(buffer, 0, length);
            System.out.println(byteBuffer.toString());
            System.out.println(new String(buffer, 0, length));
        }

    }
}
