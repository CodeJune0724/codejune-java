package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.bind.*;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import javafx.util.StringConverter;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Select<KEY, ITEM> extends BaseComponent {

    private final ComboBox<ITEM> comboBox = new ComboBox<>();

    private Function<ITEM, KEY> keyHandler = null;

    private Function<ITEM, String> valueRender = null;

    private Function<ITEM, BaseComponent> cellRender = null;

    public Select() {
        this.getStyle().addSheet("/javafx/style/select.css").addClass("select");
        this.getStyle().addSheet("/javafx/style/scroll.css");
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
                return new ListCell<>() {
                    @Override
                    protected void updateItem(ITEM item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            this.setText(null);
                            this.setGraphic(null);
                            this.setDisable(false);
                        } else {
                            if (Select.this.cellRender == null) {
                                this.setText(ObjectUtil.toString(item));
                            } else {
                                Node fxNode = Select.this.cellRender.apply(item).getFxNode();
                                this.setDisable(fxNode.isDisable());
                                this.setGraphic(fxNode);
                            }
                        }
                    }
                };
            }
        });
    }

    @Override
    public Node getFxNode() {
        return this.comboBox;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind, Supplier<?> getValue) {
        if (propertyType == BasePropertyType.VALUE) {
            this.comboBox.valueProperty().addListener((_, _, _) -> propertyBind.setObject(this.getValue()));
            return () -> this.setValue((KEY) getValue.get());
        }
        if (propertyType == BasePropertyType.DATA) {
            return () -> this.setData((List<ITEM>) getValue.get());
        }
        return super.customPropertyBind(propertyType, propertyBind, getValue);
    }

    @Override
    protected Runnable customEventBind(EventType eventType, Runnable runnable, boolean asynchronous) {
        if (eventType == BaseEventType.VALUE) {
            return () -> this.comboBox.valueProperty().addListener((_, _, _) -> asynchronousRun(runnable, asynchronous));
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
        this.comboBox.getItems().clear();
        this.comboBox.getItems().addAll(data);
    }

    public void setKeyHandler(Function<ITEM, KEY> keyHandler) {
        this.keyHandler = keyHandler;
    }

    public void setValueRender(Function<ITEM, String> valueRender) {
        this.valueRender = valueRender;
    }

    public void setCellRender(Function<ITEM, BaseComponent> cellRender) {
        this.cellRender = cellRender;
    }

}