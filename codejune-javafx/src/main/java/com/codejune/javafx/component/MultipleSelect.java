package com.codejune.javafx.component;

import com.codejune.javafx.entity.BaseEventType;
import com.codejune.javafx.entity.EventType;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.controlsfx.control.CheckComboBox;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class MultipleSelect<KEY, ITEM> extends BaseComponent {

    private final CheckComboBox<ITEM> checkComboBox = new CheckComboBox<>();

    @SuppressWarnings("unchecked")
    private Function<ITEM, KEY> keyHandler = item -> (KEY) item;

    public MultipleSelect() {
        this.getStyle().addSheet("/javafx/style/multipleSelect.css");
        this.getStyle().addSheet("/javafx/style/scroll.css");
    }

    @Override
    public Node getFxNode() {
        return this.checkComboBox;
    }

    @Override
    protected Runnable customEventBind(EventType eventType, Runnable runnable, boolean asynchronous) {
        if (eventType == BaseEventType.VALUE) {
            return () -> this.checkComboBox.getCheckModel().getCheckedItems().addListener((InvalidationListener) _ -> runnable.run());
        }
        return super.customEventBind(eventType, runnable, asynchronous);
    }

    public void setData(List<ITEM> data) {
        this.checkComboBox.getItems().clear();
        this.checkComboBox.getItems().addAll(data);
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
        ObservableList<ITEM> itemList = this.checkComboBox.getItems();
        for (int i = 0; i < itemList.size(); i++) {
            ITEM item = itemList.get(i);
            if (keyList.contains(this.keyHandler.apply(item))) {
                this.checkComboBox.getCheckModel().check(i);
            }
        }
    }

    public void setKeyHandler(Function<ITEM, KEY> keyHandler) {
        if (keyHandler == null) {
            return;
        }
        this.keyHandler = keyHandler;
    }

}