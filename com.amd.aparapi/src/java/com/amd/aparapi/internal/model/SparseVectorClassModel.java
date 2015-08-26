package com.amd.aparapi.internal.model;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import com.amd.aparapi.internal.instruction.InstructionSet.TypeSpec;
import com.amd.aparapi.internal.exception.AparapiException;
import com.amd.aparapi.internal.model.HardCodedMethodModel.MethodDefGenerator;
import com.amd.aparapi.internal.writer.KernelWriter;

public class SparseVectorClassModel extends HardCodedClassModel {
    private static final String className = "org.apache.spark.mllib.linalg.SparseVector";
    private static Class<?> clz;
    static {
        try {
            clz = Class.forName(className);
        } catch (ClassNotFoundException c) {
            throw new RuntimeException(c);
        }
    }

    private SparseVectorClassModel(List<HardCodedMethodModel> methods, List<AllFieldInfo> fields) {
        super(clz, methods, fields);
        initMethodOwners();
    }

    @Override
    public String getDescriptor() {
        return "Lorg/apache/spark/mllib/linalg/SparseVector";
    }

    @Override
    public boolean merge(HardCodedClassModel other) {
        return (other instanceof SparseVectorClassModel);
    }

    public static SparseVectorClassModel create(final int tiling) {
        List<HardCodedMethodModel> methods = new ArrayList<HardCodedMethodModel>();

        MethodDefGenerator sizeGen = new MethodDefGenerator<SparseVectorClassModel>() {
            @Override
            public String getMethodDef(HardCodedMethodModel method,
                    SparseVectorClassModel classModel, KernelWriter writer) {
                String owner = method.getOwnerClassMangledName();

                StringBuilder sb = new StringBuilder();

                sb.append("static int " + method.getName() + "(__global " + owner + " *this) {\n");
                sb.append("    return (this->size);\n");
                sb.append("}");

                return sb.toString();
            }
        };
        methods.add(new HardCodedMethodModel("size", "()I", sizeGen, false, null));

        MethodDefGenerator indicesGen = new MethodDefGenerator<SparseVectorClassModel>() {
            @Override
            public String getMethodDef(HardCodedMethodModel method,
                    SparseVectorClassModel classModel, KernelWriter writer) {
                String owner = method.getOwnerClassMangledName();

                StringBuilder sb = new StringBuilder();
                sb.append("static __global int *" + method.getName() + "(__global " +
                        owner + " *this) {\n");
                sb.append("    return (this->indices);\n");
                sb.append("}");
                return sb.toString();
            }
        };
        methods.add(new HardCodedMethodModel("indices", "()[I", indicesGen, false, null));

        MethodDefGenerator valuesGen = new MethodDefGenerator<SparseVectorClassModel>() {
            @Override
            public String getMethodDef(HardCodedMethodModel method,
                    SparseVectorClassModel classModel, KernelWriter writer) {
                String owner = method.getOwnerClassMangledName();

                StringBuilder sb = new StringBuilder();
                sb.append("static __global double *" + method.getName() + "(__global " +
                        owner + " *this) {\n");
                sb.append("    return (this->values);\n");
                sb.append("}");
                return sb.toString();
            }
        };
        methods.add(new HardCodedMethodModel("values", "()[D", valuesGen, false, null));

        List<AllFieldInfo> fields = new ArrayList<AllFieldInfo>(2);
        fields.add(new AllFieldInfo("values", "[D", "double*", -1));
        fields.add(new AllFieldInfo("indices", "[I", "int*", -1));
        fields.add(new AllFieldInfo("size", "I", "int", -1));

        return new SparseVectorClassModel(methods, fields);
    }

    @Override
    public List<String> getNestedTypeDescs() {
        return new ArrayList<String>(0);
    }

    @Override
    public String getMangledClassName() {
        return className.replace('.', '_');
    }

   @Override
   public boolean classNameMatches(String className) {
      return className.startsWith(className);
   }

   @Override
   public int calcTotalStructSize(Entrypoint entryPoint) {
       /*
        * Size of the pointer to values + size of the pointer to indices + size
        * of the integer size
        */
       final int pointerSize = Integer.parseInt(entryPoint.getConfig().get(
                   Entrypoint.clDevicePointerSize));
       return (pointerSize + pointerSize + 4);
   }

   @Override
   public String toString() {
       return className;
   }
}
