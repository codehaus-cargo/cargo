package org.codehaus.cargo.module.merge;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;

public class DocumentMerger implements MergeProcessor {
    private List<Document> documents = new ArrayList<>();

    @Override
    public void addMergeItem(Object mergeItem) throws MergeException {
        if (mergeItem instanceof Document) {
            this.documents.add((Document) mergeItem);
        } else {
            throw new MergeException("DocumentMerger can only merge Documents");
        }
    }

    @Override
    public Object performMerge() {
        if (this.documents.isEmpty()) {
            return null;
        }

        DocumentMergeStrategy mergeStrategy = new DefaultDocumentMergeStrategy();

        Document doc = this.documents.get(0);
        for (int i = 1; i < this.documents.size(); i++) {
            Document right = this.documents.get(i);
            doc = mergeStrategy.mergeDocuments(doc, right);
        }

        return doc;
    }

    // Extracted strategy class for merging documents
    private interface DocumentMergeStrategy {
        Document mergeDocuments(Document left, Document right);
    }

    // Default strategy that adds nodes from the right into the left
    private static class DefaultDocumentMergeStrategy implements DocumentMergeStrategy {
        @Override
        public Document mergeDocuments(Document left, Document right) {
            List<Content> children = new ArrayList<>(right.getRootElement().getContent());
            Document tempLeft = (Document) left.clone();

            for (Content node : children) {
                if (node instanceof Element) {
                    Content clone = ((Element) node).detach();
                    tempLeft.getRootElement().addContent(clone);
                }
            }

            return tempLeft;
        }
    }
}

