package com.google.android.apps.translate.assistant;

import android.app.assist.AssistStructure;
import android.app.assist.AssistStructure.ViewNode;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;
import android.util.Log;

public class TranslateSession extends VoiceInteractionSession {

    public TranslateSession(Context context) {
        super(context);
        Log.d("GOTranslate", "TranslateSession created");
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, android.app.assist.AssistContent content) {
        Log.d("GOTranslate", "=== ASSIST START ===");
        
        try {
            // 1. SPRÓBUJ ZNALEŹĆ TEKST W ASSISTCONTENT (nowsze API)
            if (content != null) {
                Log.d("GOTranslate", "Checking AssistContent...");
                
                Intent intent = content.getIntent();
                if (intent != null) {
                    Log.d("GOTranslate", "AssistContent Intent: " + intent.getAction());
                    if (intent.hasExtra(Intent.EXTRA_PROCESS_TEXT)) {
                        String processText = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
                        if (!TextUtils.isEmpty(processText)) {
                            Log.d("GOTranslate", "FOUND TEXT in AssistContent EXTRA_PROCESS_TEXT: " + processText);
                            redirectToTranslateActivity(processText);
                            finish();
                            return;
                        }
                    }
                    if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                        String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
                        if (!TextUtils.isEmpty(extraText)) {
                            Log.d("GOTranslate", "FOUND TEXT in AssistContent EXTRA_TEXT: " + extraText);
                            redirectToTranslateActivity(extraText);
                            finish();
                            return;
                        }
                    }
                }
                
                ClipData clipData = content.getClipData();
                if (clipData != null && clipData.getItemCount() > 0) {
                    CharSequence text = clipData.getItemAt(0).getText();
                    if (!TextUtils.isEmpty(text)) {
                        Log.d("GOTranslate", "FOUND TEXT in AssistContent ClipData: " + text);
                        redirectToTranslateActivity(text.toString());
                        finish();
                        return;
                    }
                }
            }

            // 2. SPRÓBUJ ZNALEŹĆ TEKST W BUNDLE
            if (data != null) {
                Log.d("GOTranslate", "Checking Bundle data...");
                Log.d("GOTranslate", "Bundle keys: " + data.keySet());
                
                String text = data.getString(Intent.EXTRA_PROCESS_TEXT);
                if (!TextUtils.isEmpty(text)) {
                    Log.d("GOTranslate", "FOUND TEXT in Bundle EXTRA_PROCESS_TEXT: " + text);
                    redirectToTranslateActivity(text);
                    finish();
                    return;
                }
                
                text = data.getString(Intent.EXTRA_TEXT);
                if (!TextUtils.isEmpty(text)) {
                    Log.d("GOTranslate", "FOUND TEXT in Bundle EXTRA_TEXT: " + text);
                    redirectToTranslateActivity(text);
                    finish();
                    return;
                }
            }

            // 3. DOPIERO POTEM SPRÓBUJ ASSISTSTRUCTURE
            Log.d("GOTranslate", "Checking AssistStructure...");
            String selectedText = extractSelectedText(structure);
            
            if (!TextUtils.isEmpty(selectedText)) {
                Log.d("GOTranslate", "SELECTED TEXT from structure: " + selectedText);
                redirectToTranslateActivity(selectedText);
            } else {
                Log.d("GOTranslate", "No text selected in structure - checking clipboard");
                String clipboardText = getTextFromClipboard();
                if (!TextUtils.isEmpty(clipboardText)) {
                    Log.d("GOTranslate", "SELECTED TEXT from clipboard: " + clipboardText);
                    redirectToTranslateActivity(clipboardText);
                } else {
                    Log.d("GOTranslate", "No text in clipboard either");
                }
            }
            
        } catch (Exception e) {
            Log.e("GOTranslate", "Error in onHandleAssist", e);
        } finally {
            Log.d("GOTranslate", "=== ASSIST END ===");
            finish();
        }
    }

    private String extractSelectedText(AssistStructure structure) {
        if (structure == null) {
            Log.w("GOTranslate", "extractSelectedText: structure is null");
            return null;
        }

        Log.d("GOTranslate", "=== ASSIST STRUCTURE DEBUG ===");
        Log.d("GOTranslate", "Window nodes: " + structure.getWindowNodeCount());
        
        StringBuilder debugInfo = new StringBuilder();
        
        for (int i = 0; i < structure.getWindowNodeCount(); i++) {
            AssistStructure.WindowNode window = structure.getWindowNodeAt(i);
            debugInfo.append("Window ").append(i).append(": '").append(window.getTitle()).append("'\n");
            
            ViewNode root = window.getRootViewNode();
            if (root != null) {
                String text = traverseNodeWithDebug(root, debugInfo, 0);
                if (text != null) {
                    Log.d("GOTranslate", "FOUND TEXT: " + text);
                    Log.d("GOTranslate", "DEBUG INFO:\n" + debugInfo.toString());
                    return text;
                }
            }
        }
        
        Log.d("GOTranslate", "DEBUG INFO:\n" + debugInfo.toString());
        Log.d("GOTranslate", "No selected text found in any window");
        return null;
    }

    private String traverseNodeWithDebug(ViewNode node, StringBuilder debug, int depth) {
        if (node == null) return null;

        String indent = "  ".repeat(depth);
        CharSequence text = node.getText();
        
        if (text != null) {
            debug.append(indent).append("Node: '").append(text).append("'");
            debug.append(" [selection: ").append(node.getTextSelectionStart()).append("-").append(node.getTextSelectionEnd()).append("]");
            debug.append(" class: ").append(node.getClassName()).append("\n");
            
            int start = node.getTextSelectionStart();
            int end = node.getTextSelectionEnd();
            
            if (start >= 0 && end > start && end <= text.length()) {
                String selected = text.subSequence(start, end).toString();
                debug.append(indent).append(">>> SELECTED: '").append(selected).append("'\n");
                return selected;
            }
        } else {
            debug.append(indent).append("Node: no text, class: ").append(node.getClassName()).append("\n");
        }

        if (node.getHint() != null) {
            debug.append(indent).append("Hint: '").append(node.getHint()).append("'\n");
        }
        if (node.getContentDescription() != null) {
            debug.append(indent).append("ContentDesc: '").append(node.getContentDescription()).append("'\n");
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            String result = traverseNodeWithDebug(node.getChildAt(i), debug, depth + 1);
            if (result != null) return result;
        }
        
        return null;
    }

    private String getTextFromClipboard() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clipData = clipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    CharSequence text = clipData.getItemAt(0).getText();
                    if (!TextUtils.isEmpty(text)) {
                        return text.toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("GOTranslate", "Error getting text from clipboard", e);
        }
        return null;
    }

    private void redirectToTranslateActivity(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setComponent(new ComponentName(
            getContext().getPackageName(),
            "com.google.android.apps.translate.TranslateActivity"
        ));
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        getContext().startActivity(intent);
        Log.d("GOTranslate", "Successfully redirected to TranslateActivity with text: " + text);
    }
}
