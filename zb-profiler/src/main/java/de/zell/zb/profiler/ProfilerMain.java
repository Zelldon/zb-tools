package de.zell.zb.profiler;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.sun.tools.attach.*;

/**
 */
public class ProfilerMain
{
    public static void premain(String args, Instrumentation inst)
    {
        inst.addTransformer(new Transformer(args));
    }


    private static final String DESCRIPTORS = "[%d]: %s";

    public static final String PATH = "/home/zell/git-repos/public/zb-tools/zb-profiler/target/zb-profiler-1.0-SNAPSHOT-jar-with-dependencies.jar";


    public static void main(String args[])
    {
        final List<VirtualMachineDescriptor> descriptors = VirtualMachine.list();

        if (!descriptors.isEmpty())
        {
//            System.out.println("Please choose an descriptor:");
//            for (int i = 0; i < descriptors.size(); i++)
//            {
//                System.out.println(String.format(DESCRIPTORS, i, descriptors.get(i).displayName()));
//            }

            // read input
//            final int choosen = 1;


            final List<VirtualMachineDescriptor> brokers = descriptors.stream()
                                                   .filter((descriptor) -> descriptor.displayName()
                                                                                     .contains("Broker"))
                                                   .limit(1)
                                                   .collect(Collectors.toList());

            if (!brokers.isEmpty())
            {


                final VirtualMachineDescriptor brokerDescriptor = brokers.get(0);

                try
                {
                    final VirtualMachine attach = VirtualMachine.attach(brokerDescriptor.id());

                    System.out.println("Agent path: " + PATH);
                    // load profiler
                    attach.loadAgent(PATH);

                    // DO

                    try (Scanner reader = new Scanner(System.in))
                    {
                        boolean end = false;
                        while (!end)
                        {
                            final String line = reader.next();
                            end = line.equalsIgnoreCase("exit");
                        }
                    }

                    System.out.println("Detach profiler!");
                    // end
                    attach.detach();
                }
                catch (AgentInitializationException aie)
                {
                    System.err.println("AgentInitializationException: " + aie.getMessage());
                    aie.printStackTrace();
                }
                catch (AgentLoadException ale)
                {
                    System.err.println("AgentLoadException: " + ale.getMessage());
                    ale.printStackTrace();
                }
                catch (AttachNotSupportedException anse)
                {
                    System.err.println("AttachNotSupportedException: " + anse.getMessage());
                    anse.printStackTrace();
                }
                catch (IOException ioe)
                {
                    System.err.println("IOException: " + ioe.getMessage());
                    ioe.printStackTrace();
                }
            }
            else
            {
                System.out.println("No active Broker. Bye!");
            }
        }
        else
        {
            System.out.println("Bye!");
        }

    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation)
    {
        System.out.println("Hooked into JVM! LETS GO!");



        final Transformer transformer = new Transformer("io/zeebe");

        final List<Class> classes = new ArrayList<>();
        try
        {
            classes.add(Class.forName("io.zeebe.util.state.StateMachine"));
            classes.add(Class.forName("io.zeebe.util.state.StateMachineAgent"));
            classes.add(Class.forName("io.zeebe.util.actor.ActorRunner"));
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        instrumentation.addTransformer(transformer, true);

//        final Class[] allLoadedClasses = instrumentation.getAllLoadedClasses();
        try
        {
//            System.out.println("Loaded classes: " + allLoadedClasses.length);
//            final List<Class> collect = Arrays.stream(allLoadedClasses)
//                                              .filter((clazz) -> instrumentation.isModifiableClass(clazz))
//                                              .collect(Collectors.toList());
//
//            final Class[] modifiableClasses = collect.toArray(new Class[collect.size()]);
//            System.out.println("Modifiable classes: " + allLoadedClasses.length);

//            instrumentation.retransformClasses(modifiableClasses);
            if (!classes.isEmpty())
            {
                System.out.println("Retransform " + classes);
                instrumentation.retransformClasses(classes.toArray(new Class[classes.size()]));
            }
        }
        catch (UnmodifiableClassException e)
        {
            System.out.println("Class was unmodifiable! Message: " + e.getMessage());
            e.printStackTrace();
        }

//
//        System.out.println("Retransform changed classes for detaching!");
//        // on exit
//        instrumentation.removeTransformer(transformer);
//        try
//        {
//            instrumentation.retransformClasses(allLoadedClasses);
//        }
//        catch (UnmodifiableClassException e)
//        {
//            System.out.println("Class was unmodifiable! Message: " + e.getMessage());
//            e.printStackTrace();
//        }
    }
}
