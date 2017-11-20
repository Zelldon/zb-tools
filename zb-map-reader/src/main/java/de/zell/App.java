package de.zell;

import static io.zeebe.map.BucketBufferArrayDescriptor.*;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.agrona.concurrent.UnsafeBuffer;

/**
 * Hello world!
 *
 */
public class App 
{
    protected static final String CHECKSUM_ALGORITHM = "SHA1";

    private static final int SIZE_OF_LONG = 8;
    private static final int SIZE_OF_INT = 4;


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
//        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
        UnsafeBuffer byteBuffer = new UnsafeBuffer(buffer);

        byte countOfParts = (byte) digestInputStream.read();
        System.out.println("Snapshot contains: " + countOfParts + " maps.");

        for (byte i = 0; i < countOfParts; i++)
        {
            System.out.println("Map: " + i);
            readBytes(digestInputStream, buffer, SIZE_OF_LONG);
            final long mapSize = byteBuffer.getLong(0);
            System.out.println("Map size: " + mapSize + " bytes");

            long readBytes = 0;
            readBytes(digestInputStream, buffer, SIZE_OF_INT);
            readBytes += SIZE_OF_INT;
            final int mapVersion = byteBuffer.getInt(0);
            System.out.println("Map-Version: " + mapVersion);

            readBytes(digestInputStream, buffer, SIZE_OF_INT);
            readBytes += SIZE_OF_INT;
            final int hashTableCapacity = byteBuffer.getInt(0);
            final int hashTableSize = hashTableCapacity * SIZE_OF_LONG;
            System.out.println("Hash table size: " + hashTableSize);
            System.out.println("Hash table capacity: " + hashTableCapacity);
            skipBytes(digestInputStream, hashTableSize);
            readBytes += hashTableSize;

            readBytes(digestInputStream, buffer, MAIN_BUCKET_BUFFER_HEADER_LEN);
            readBytes += MAIN_BUCKET_BUFFER_HEADER_LEN;
            final int bufferCount = byteBuffer.getInt(MAIN_BUFFER_COUNT_OFFSET);
            final int bucketCount = byteBuffer.getInt(MAIN_BUCKET_COUNT_OFFSET);
            final long blockCount = byteBuffer.getLong(MAIN_BLOCK_COUNT_OFFSET);
            final int highestBucketId = byteBuffer.getInt(MAIN_HIGHEST_BUCKET_ID);

            System.out.println("Bucket buffer count: " + bufferCount);
            System.out.println("Bucket count: " + bucketCount);
            System.out.println("Block count: " + blockCount);
            System.out.println("Highest bucket ID: " + highestBucketId);
            System.out.println();

            final long diff = mapSize - readBytes;
            skipBytes(digestInputStream, diff);
        }

    }

    private static void readBytes(InputStream in, byte[] byteArray, int toReadBytes)
    {
        int remainingToReadBytes = toReadBytes;
        int offset = 0;
        while (remainingToReadBytes > 0)
        {
            try
            {
                int readBytes = in.read(byteArray, offset, remainingToReadBytes);
                offset += readBytes;
                remainingToReadBytes -= readBytes;
            }
            catch (IOException ioe)
            {
            }
        }
    }

    private static void skipBytes(InputStream in, long toSkippedBytes)
    {
        long remainingToSkippedBytes = toSkippedBytes;
        while (remainingToSkippedBytes > 0)
        {
            try
            {
                remainingToSkippedBytes -= in.skip(remainingToSkippedBytes);
            }
            catch (IOException ioe)
            {
            }
        }
    }


}
