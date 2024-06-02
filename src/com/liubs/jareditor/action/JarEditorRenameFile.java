package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.liubs.jareditor.jarbuild.JarBuildResult;
import com.liubs.jareditor.jarbuild.JarBuilder;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.MyPathUtil;
import com.liubs.jareditor.util.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Liubsyy
 * @date 2024/6/2
 */
public class JarEditorRenameFile extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if(e.getProject() == null) {
            NoticeInfo.warning("Please open a project");
            return;
        }
        if(null == selectedFile) {
            NoticeInfo.warning("No file selected");
            return;
        }

        final String jarPath =  MyPathUtil.getJarPathFromJar(selectedFile.getPath());
        if(null == jarPath) {
            NoticeInfo.warning("This operation only in JAR !!!");
            return;
        }

        final boolean isDictionary = selectedFile.isDirectory();
        final String oldEntry =  isDictionary ?
                (MyPathUtil.getEntryPathFromJar(selectedFile.getPath()) + "/") :
                MyPathUtil.getEntryPathFromJar(selectedFile.getPath());

        String userInput = Messages.showInputDialog(
                e.getProject(),
                "Enter name for new name:",
                "Rename File",
                Messages.getQuestionIcon(),
                selectedFile.getName(),
                null  // 可选的输入校验器，如果没有则传 null
        );
        if(StringUtils.isEmpty(userInput)) {
            return;
        }


        final String newName =  isDictionary ?
                (MyPathUtil.getEntryPathFromJar(selectedFile.getParent().getPath()+"/"+userInput)+"/")
                :
                (MyPathUtil.getEntryPathFromJar(selectedFile.getParent().getPath()+"/"+userInput));

        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Renaming files in JAR...", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    JarBuilder jarBuilder = new JarBuilder(jarPath);
                    JarBuildResult jarBuildResult = jarBuilder.renameFile(oldEntry, newName, isDictionary);
                    if(!jarBuildResult.isSuccess()) {
                        NoticeInfo.error("Build jar err: \n%s",jarBuildResult.getErr());
                        return;
                    }

                    VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);

                    NoticeInfo.info("Rename success !");

                }catch (Throwable e) {
                    NoticeInfo.error("Rename files err",e);
                }
            }
        });

    }
}