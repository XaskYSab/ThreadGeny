package com.xaskysab.gcomlper;

import com.xaskysab.gan.TMode;
import com.xaskysab.gan.TjGeny;
import com.xaskysab.gan.TypeActionGeny;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;


@AutoService(Processor.class)
public class ViewInjectProcess extends AbstractProcessor {

    Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {

        Set<String> types = new LinkedHashSet<>();
        types.add(TypeActionGeny.class.getCanonicalName());

        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        try {
            Map<TypeElement, List<ExecutableElement>> tmap = handleTypeAnnotation(roundEnvironment);
            writeJavaClassWithTypeMap(tmap);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    private void writeJavaClassWithTypeMap(Map<TypeElement, List<ExecutableElement>> tmap) throws IOException {

        for (TypeElement typeElement : tmap.keySet()) {

            String pn = getPn(typeElement);

            String nc = typeElement.getSimpleName().toString() + "_";
            String nfc = pn + "." + nc;

            Writer writer = filer.createSourceFile(nfc).openWriter();

            List<String> export = new ArrayList<>();
            export.add(" android.os.AsyncTask");
            export.add(" android.os.Handler");
            export.add(" android.os.Looper");
            export.add(" com.xaskysab.gcomlper.ThreadGeny");

            writeImport(writer, pn, export, nc + " extends " + typeElement.asType().toString());
            writeStartBody(writer);

            for (ExecutableElement executableElement : tmap.get(typeElement)) {
                TjGeny methodActionGeny = executableElement.getAnnotation(TjGeny.class);
                TMode value = methodActionGeny.value();

                if (value == TMode.MAIN) {
                    writeMainMethod(writer, typeElement, executableElement);
                } else if (value == TMode.Async) {
                    writeNewThread(writer, typeElement, executableElement);
                }
            }

            writeEndBody(writer);
            writer.close();


        }
    }

    private void writeNewThread(Writer writer, TypeElement type, ExecutableElement executable) throws IOException {


        writer.write(String.format("public " + executable.getReturnType().toString() + " " + executable.getSimpleName()));
        writer.write("(");


        String pa = "";
        List<TypeParameterElement> types = (List<TypeParameterElement>) executable.getTypeParameters();

        int size = executable.getParameters().size();
        for (int i = 0; i < size; i++) {
            pa += types.get(i).asType().toString() + " " + executable.getParameters().get(i).getSimpleName() + " ";
        }
        writer.write(pa);

        writer.write(")");

        writeStartBody(writer);


        pa = "";
        for (int i = 0; i < size; i++) {
            pa += " " + executable.getParameters().get(i).getSimpleName() + " ";
        }

        if (executable.getReturnType().toString().equals("void")) {

            writer.write("  AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() { @Override" + "  public void run() {" +
                    "                " + type.getSimpleName().toString() + "_.super." + executable.getSimpleName() + "(" + pa + ");" +
                    "                   }" +
                    "        });");


        } else {
            writer.write("try {" + " return  new AsyncTask<Void,Void," + executable.getReturnType().toString() + ">(){" +
                    " @Override " + " protected " + executable.getReturnType().toString() + " doInBackground(Void... params) {" +
                    " return  " + type.getSimpleName().toString() + "_.super." + executable.getSimpleName() + "(" + pa + ");" + "   }" +
                    "            }.execute().get();" + "        } catch (Exception e) {" +
                    "   e.printStackTrace();" + " return null;" +
                    "     }");
        }

        writeEndBody(writer);
    }

    private void writeMainMethod(Writer writer, TypeElement type, ExecutableElement executable) throws IOException {

        writer.write("public " + executable.getReturnType().toString() + " " + executable.getSimpleName());
        writer.write("(");

        String pa = "";
        List<TypeParameterElement> types = (List<TypeParameterElement>) executable.getTypeParameters();

        int size = executable.getParameters().size();
        for (int i = 0; i < size; i++) {
            pa += types.get(i).asType().toString() + " " + executable.getParameters().get(i).getSimpleName() + " ";
        }
        writer.write(pa);

        writer.write(")");

        writeStartBody(writer);


        pa = "";
        for (int i = 0; i < size; i++) {
            pa += " " + executable.getParameters().get(i).getSimpleName() + " ";
        }

        if (executable.getReturnType().toString().equals("void")) {
            writer.write("Runnable _runnable = new Runnable() {" +
                    "            @Override " + "public void run() {" + type.getSimpleName().toString() + "_.super." + executable.getSimpleName() + "(" + pa + ");" +
                    "            }" +
                    "        };");
            writer.write("ThreadGeny.run(_runnable);");

        } else {

            throw new RuntimeException("method must be void returnType");
        }
        writeEndBody(writer);
    }

    public String getPn(Element element) {
        return processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
    }


    private void writeImport(Writer writer, String pn, List<String> importClass, String nc) throws IOException {
        writer.write("package " + pn + " ;");

        for (String importCls : importClass) {
            writer.write("import " + importCls + ";");
        }
        writer.write("public class " + nc);
    }

    private void writeStartBody(Writer writer) throws IOException {

        writer.write("{");
    }


    private Map<TypeElement, List<ExecutableElement>> handleTypeAnnotation(RoundEnvironment roundEnvironment) throws IOException {


        Set<TypeElement> types = (Set<TypeElement>) roundEnvironment.getElementsAnnotatedWith(TypeActionGeny.class);

        Map<TypeElement, List<ExecutableElement>> map = new HashMap<>();


        for (TypeElement typeElement : types) {


            String typeName = typeElement.asType().toString();

            Set<ExecutableElement> executables = (Set<ExecutableElement>) roundEnvironment.getElementsAnnotatedWith(TjGeny.class);

            for (ExecutableElement executable : executables) {

                if (typeName.equals(executable.getEnclosingElement().asType().toString())) {

                    List<ExecutableElement> executableList = map.get(typeElement);
                    if (executableList == null || executableList.isEmpty()) {
                        executableList = new ArrayList<>();
                        map.put(typeElement, executableList);
                    }
                    map.get(typeElement).add(executable);
                }

            }

        }

        return map;
    }


    private void writeEndBody(Writer writer) throws IOException {

        writer.write(" }");

    }


}













