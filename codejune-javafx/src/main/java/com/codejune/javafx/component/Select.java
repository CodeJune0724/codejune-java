package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.Application;
import com.codejune.javafx.bind.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import javafx.util.StringConverter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Select<KEY, ITEM> extends BaseComponent {

    private final ComboBox<ITEM> comboBox = new ComboBox<>();

    private Function<ITEM, KEY> keyHandler = null;

    private Function<ITEM, String> valueRender = null;

    private Function<ITEM, BaseComponent> cellRender = null;

    private final ReadOnlyObjectWrapper<KEY> selectedKey = new ReadOnlyObjectWrapper<>();

    private boolean updatingItems;

    private final Set<RenderedCell> cells = Collections.newSetFromMap(new WeakHashMap<>());

    public Select() {
        this.getStyle().addClass("select");
        this.observe(this.comboBox.valueProperty(), (_, _, _) -> {
            if (!this.updatingItems) this.selectedKey.set(this.getValue());
        });
        this.comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ITEM item) {
                if (item == null) {
                    return null;
                }
                if (Select.this.valueRender != null) {
                    return Select.this.valueRender.apply(item);
                }
                return ObjectUtil.toString(item);
            }
            @Override
            public ITEM fromString(String value) {
                for (ITEM item : Select.this.comboBox.getItems()) {
                    if (ObjectUtil.equals(value, Select.this.valueRender == null ? ObjectUtil.toString(item) : Select.this.valueRender.apply(item))) {
                        return item;
                    }
                }
                return null;
            }
        });
        this.comboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<ITEM> call(ListView<ITEM> itemListView) {
                RenderedCell cell = new RenderedCell();
                Select.this.cells.add(cell);
                return cell;
            }
        });
    }

    private final class RenderedCell extends ListCell<ITEM> {
        private BaseComponent component;

        private void clearComponent() {
            this.setGraphic(null);
            if (this.component != null) {
                this.component.delete();
                this.component = null;
            }
        }

        @Override
        protected void updateItem(ITEM item, boolean empty) {
            super.updateItem(item, empty);
            this.clearComponent();
            this.setText(null);
            this.setDisable(false);
            if (empty || item == null || Select.this.isDeleted()) return;
            if (Select.this.cellRender == null) {
                this.setText(ObjectUtil.toString(item));
            } else {
                this.component = Select.this.cellRender.apply(item);
                if (this.component != null) {
                    Select.this.registerChild(this.component);
                    this.setDisable(this.component.getFxNode().isDisable());
                    this.setGraphic(this.component.getFxNode());
                }
            }
        }
    }

    @Override
    protected void onDelete() {
        this.comboBox.hide();
        for (RenderedCell cell : new ArrayList<>(this.cells)) cell.clearComponent();
        this.cells.clear();
        this.cellRender = null;
    }

    @Override
    public Node getFxNode() {
        return this.comboBox;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind, Supplier<?> getValue) {
        if (propertyType == BasePropertyType.VALUE) {
            this.observe(this.selectedKey, (_, _, newValue) -> {
                if (getValue == null) {
                    propertyBind.setObject(newValue);
                }
            });
            return () -> this.setValue((KEY) getPropertyBindValue(propertyBind, getValue));
        }
        if (propertyType == BasePropertyType.DATA) {
            return () -> this.setData((List<ITEM>) getPropertyBindValue(propertyBind, getValue));
        }
        return super.customPropertyBind(propertyType, propertyBind, getValue);
    }

    @Override
    protected Runnable customEventBind(EventType eventType, Runnable runnable, boolean asynchronous) {
        if (eventType == BaseEventType.VALUE) {
            return () -> this.observe(this.selectedKey, (_, _, _) -> Application.asynchronousRun(runnable, asynchronous));
        }
        return super.customEventBind(eventType, runnable, asynchronous);
    }

    public void setPlaceholder(String placeholder) {
        this.comboBox.setPromptText(placeholder);
    }

    public void setValue(KEY key) {
        for (ITEM item : this.comboBox.getItems()) {
            if (ObjectUtil.equals(key, this.keyHandler == null ? item : this.keyHandler.apply(item))) {
                this.comboBox.setValue(item);
                return;
            }
        }
        this.comboBox.setValue(null);
    }

    @SuppressWarnings("unchecked")
    public KEY getValue() {
        ITEM item = this.comboBox.getValue();
        if (item == null) {
            return null;
        }
        return this.keyHandler == null ? (KEY) item : this.keyHandler.apply(item);
    }

    public void setData(List<ITEM> data) {
        List<ITEM> snapshot = data == null ? List.of() : new ArrayList<>(data);
        KEY previousKey = this.getValue();
        this.updatingItems = true;
        try {
            this.comboBox.getItems().setAll(snapshot);
            this.setValue(previousKey);
        } finally {
            this.updatingItems = false;
        }
        this.selectedKey.set(this.getValue());
    }

    public void setKeyHandler(Function<ITEM, KEY> keyHandler) {
        this.keyHandler = keyHandler;
        this.selectedKey.set(this.getValue());
    }

    public void setValueRender(Function<ITEM, String> valueRender) {
        this.valueRender = valueRender;
    }

    public void setCellRender(Function<ITEM, BaseComponent> cellRender) {
        this.cellRender = cellRender;
    }

}