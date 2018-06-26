package test;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiElementFactoryImpl;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.Scope;

import java.util.LinkedList;
import java.util.List;

public class TestAction extends AnAction {

    public TestAction() {
        super("Hello!");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        System.out.println("ciao");

        String cat = "okfile";
        VirtualFile file = null;
        try {
            cat += " "+project.getBasePath()+"  ";

            file = project.getProjectFile().findChild("AndroidManifest.xml");
        } catch (Exception exc) {
            cat = "no_ok_file";
        }
        try {
            cat += "  "+file.getPath()+" ";
            cat += file.isDirectory()? " isDir" : " notisdir";
            FileType fileType = file.getFileType();
            //String extension = fileType.getDefaultExtension();
            cat += " with extension"+file.getExtension();
        } catch (Exception exc) {
            cat += " and no extensionzzzzz";
        }

        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        //psiFile.ele

        PsiFile[] listpsi = FilenameIndex.getFilesByName(project, "AndroidManifest.xml", GlobalSearchScope.projectScope(project));

        List<String> stringList = new LinkedList<>();
        for (PsiFile file1 : listpsi) {
            cat += "\n"+file1.getVirtualFile().getPath()+"\n";
            PsiElement psiElement = file1.findElementAt(0);
            XmlFile xmlFile = (XmlFile) file1;
            try {
                String package_ = xmlFile.getRootTag().getAttribute("package").getValue();
                XmlTag applicationTag = xmlFile.getRootTag().findFirstSubTag("application");

                XmlTag[] activityTags = applicationTag.findSubTags("activity");

                for (XmlTag tag : activityTags) {
                    cat += "\n"+tag.getAttribute("android:name").getValue();
                    try {
                        String[] names = tag.getAttribute("android:name").getValue().split("\\.");
                        String name = names[names.length - 1] + ".java";
                        stringList.add(name);

                        cat += "    " + name;
                    } catch (Exception e2) {
                        cat += "    "+e2.toString();
                    }

                }
            } catch (Exception e1) {
                e1.printStackTrace();
                cat += "  error in tag reading\n";
            }
        }

        for (String javaActivityName: stringList) {
            PsiFile[] listActJava = FilenameIndex.getFilesByName(project, javaActivityName, GlobalSearchScope.projectScope(project));
            for (PsiFile actJava: listActJava) {
                if (actJava instanceof PsiJavaFile) {
                    PsiJavaFile javaFile = (PsiJavaFile) actJava;
                    PsiClass psiClass = javaFile.getClasses()[0];
                    cat += "   "+psiClass.getQualifiedName()+"   ";
                    PsiMethod[] psiMethods = psiClass.findMethodsByName("onResume", false);

                    cat += "   osResume()  ";
                    cat += psiMethods.length > 0 ? "found\n" : "not found\n";

                    if (psiMethods.length > 0) {

                        try {
                            //PsiCommentImpl comment = new PsiCommentImpl(
                            //        JavaTokenType.C_STYLE_COMMENT, "//CIAO, TI HO INSERITO?");

                            //        psiMethods[0].getBody().add();



                            cat += psiMethods[0].getBody().isWritable()? "writable" : "not wrtable";
                            cat += "\n";
                            //PsiElement last = psiMethods[0].getBody().getLastBodyElement();

                            //comment.getParent().replace(psiMethods[0].getBody());

                            //psiMethods[0].getBody().addBefore(
                            //        comment, last
                            //);

                            //cat += psiMethods[0].getBody().isEmpty()? "\nonResume empyy\n" : "\nonResume not empty";

                            //if (!psiMethods[0].getBody().isEmpty()) {
                            //psiMethods[0].getBody().add(
                                    //PsiElementFactory.SERVICE.getInstance(project).createExpressionFromText("//Ti inserisco", null)
                            //        PsiElementFactory.SERVICE.getInstance(project).createCommentFromText("//ciao", null)
                            //);
                            //}

                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                /*psiClass.add(
                                        PsiElementFactory.SERVICE.getInstance(project).createMethodFromText(
                                                "public void onNull(){}", psiClass
                                        )
                                );*/
                                psiMethods[0].getBody().add(
                                        //PsiElementFactory.SERVICE.getInstance(project).createExpressionFromText("System.out.println(\"ciao amico\");", psiClass)
                                        PsiElementFactory.SERVICE.getInstance(project).createStatementFromText("System.out.println(\"ciao amico\");", psiClass)
                                );
                            });
                            /*psiClass.add(
                                    PsiElementFactory.SERVICE.getInstance(project).createMethodFromText(
                                            "public void onNull(){}", psiClass
                                    )
                            );*/



                        } catch (Exception e3) {
                            cat += e3.toString()+"\n";
                        }

                    } else { //NO onResume METHOD FOUND
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            psiClass.add(
                                    PsiElementFactory.SERVICE.getInstance(project).createMethodFromText("@Override\n" +
                                            "protected void onResume(){\n" +
                                            "super.onResume();\n" +
                                            "PrefetchingLib.setCurrentActivity(this);\n" +
                                            "}", psiClass)
                            );
                        });
                    }

                }
            }
        }

        Messages.showMessageDialog("Hello\t"+cat, "World", Messages.getInformationIcon());

    }
}
