package de.zell.zb.profiler;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.*;
import javassist.util.HotSwapper;

/**
 *
 */
public class Transformer implements ClassFileTransformer
{
    private final ClassPool classPool;
    private final String filter;

    public Transformer(String filter)
    {
        classPool = ClassPool.getDefault();
        System.out.println("Filter " + filter);
        this.filter = filter;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
        throws IllegalClassFormatException
    {
        byte [] newClassFile = null;
        try
        {
//            if (className != null && !className.contains("zell") &&
//                !className.contains("java") && !className.contains("sun") &&
//                (filter == null || className.startsWith(filter)))
            if (className.contains("TestMain"))
            {
                System.out.println("Transform: " + className);
                final String correctClassName = className.replace('/', '.');
                final CtClass cc = classPool.get(correctClassName);
                if (!cc.isInterface() && !cc.isPrimitive() && !cc.isFrozen())
                {
                    newClassFile = transformClass(correctClassName, cc);

                }
                else
                {
                    System.out.println("Was not transformed! IS frozen: " + cc.isFrozen());
                    newClassFile = transformClass(correctClassName, cc);
                }
            }

            if (newClassFile != null)
            {
                final HotSwapper swapper = new HotSwapper(1044);
                swapper.reload(className, newClassFile);
            }
        }
        catch (IOException ioe)
        {
            System.err.println(ioe.getMessage() + " transforming class " + className + "; returning uninstrumented    class");
        }
        catch (NotFoundException nfe)
        {
            System.err.println(nfe.getMessage() + " transforming class " + className + "; returning uninstrumented class");
        }
        catch (CannotCompileException cce)
        {
            System.err.println(cce.getMessage() + " transforming class " + className + "; returning uninstrumented class");
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        return null;
    }

    private byte[] transformClass(String correctClassName, CtClass cc) throws CannotCompileException, IOException
    {
        final CtMethod[] methods = cc.getMethods();
        for (int k = 0; k < methods.length; k++)
        {
            final CtMethod method = methods[k];
            if (method.getLongName().startsWith(correctClassName))
            {
//                method.addLocalVariable("_startTime", CtClass.longType);
//                method.insertBefore("_startTime = System.nanoTime();");
//
//                method.insertAfter("System.out.println(\"Executing: " +
//                                       method.getLongName() +
//                                       " takes \" + (System.nanoTime() - _startTime));");
//                System.out.println("Method " + method.getLongName() + "was transformed!");

                method.insertAfter("System.out.println(\"This was not there before.\");");
            }
            else
            {


                System.out.println("Method " + method.getLongName() + "was not transformed!");
            }
        }

        // return the new bytecode array:
        final byte[] newClassfileBuffer = cc.toBytecode();
        return newClassfileBuffer;
    }
}
