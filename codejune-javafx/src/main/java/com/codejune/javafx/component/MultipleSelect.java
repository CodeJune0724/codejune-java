package com.codejune.javafx.component;

import com.codejune.javafx.bind.BaseEventType;
import com.codejune.javafx.bind.EventType;
import com.codejune.javafx.Application;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.controlsfx.control.CheckComboBox;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public final class MultipleSelect<KEY, ITEM> extends BaseComponent {

    private final CheckComboBox<ITEM> checkComboBox = new CheckComboBox<>();

    @SuppressWarnings("unchecked")
    private Function<ITEM, KEY> keyHandler = item -> (KEY) item;

    private boolean updatingSelection;

    private final List<Runnable> valueListeners = new ArrayList<>();

    public MultipleSelect() {
        this.checkComboBox.getCheckModel().getCheckedItems().addListener((InvalidationListener) _ -> {
            if (!this.updatingSelection) this.notifyValueListeners();
        });
    }

    private void notifyValueListeners() {
        if (!this.isDeleted()) new ArrayList<>(this.valueListeners).forEach(Runnable::run);
    }

    @Override
    protected void onDelete() {
        this.valueListeners.clear();
    }

    @Override
    public Node getFxNode() {
        return this.checkComboBox;
    }

    @Override
    protected Runnable customEventBind(EventType eventType, Runnable runnable, boolean asynchronous) {
        if (eventType == BaseEventType.VALUE) {
            return () -> this.valueListeners.add(() -> Application.asynchronousRun(runnable, asynchronous));
        }
        return super.customEventBind(eventType, runnable, asynchronous);
    }

    public void setData(List<ITEM> data) {
        List<ITEM> snapshot = data == null ? List.of() : new ArrayList<>(data);
        this.updateSelection(() -> this.checkComboBox.getItems().setAll(snapshot));
    }

    public List<KEY> getValue() {
        List<KEY> result = new ArrayList<>();
        ObservableList<Integer> checkedIndices = this.checkComboBox.getCheckModel().getCheckedIndices();
        ObservableList<ITEM> itemList = this.checkComboBox.getItems();
        for (Integer index : checkedIndices) {
            ITEM item = itemList.get(index);
            result.add(this.keyHandler.apply(item));
        }
        return result;
    }

    public void setValue(List<KEY> keyList) {
        Set<KEY> keys = new HashSet<>(keyList == null ? List.of() : keyList);
        this.updateSelection(() -> {
            ObservableList<ITEM> itemList = this.checkComboBox.getItems();
            for (int i = 0; i < itemList.size(); i++) {
                if (keys.contains(this.keyHandler.apply(itemList.get(i)))) {
                    this.checkComboBox.getCheckModel().check(i);
                } else {
                    this.checkComboBox.getCheckModel().clearCheck(i);
                }
            }
        });
    }

    private void updateSelection(Runnable update) {
        List<KEY> previous = this.getValue();
        this.updatingSelection = true;
        try {
            update.run();
        } finally {
            this.updatingSelection = false;
        }
        if (!previous.equals(this.getValue())) this.notifyValueListeners();
    }

    public void setKeyHandler(Function<ITEM, KEY> keyHandler) {
        if (keyHandler == null) {
            return;
        }
        this.keyHandler = keyHandler;
    }

}