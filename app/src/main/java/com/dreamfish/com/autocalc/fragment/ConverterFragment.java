package com.dreamfish.com.autocalc.fragment;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.dreamfish.com.autocalc.R;
import com.dreamfish.com.autocalc.core.AutoCalc;
import com.dreamfish.com.autocalc.item.converter.ConverterData;
import com.dreamfish.com.autocalc.item.converter.ConverterDataGroup;
import com.dreamfish.com.autocalc.item.converter.ConverterItem;
import com.dreamfish.com.autocalc.item.ConvertsListItem;
import com.dreamfish.com.autocalc.item.FunctionsListItem;
import com.dreamfish.com.autocalc.item.adapter.ConvertsListAdapter;
import com.dreamfish.com.autocalc.item.adapter.FunctionsListAdapter;
import com.dreamfish.com.autocalc.utils.AlertDialogTool;
import com.dreamfish.com.autocalc.utils.PixelTool;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class ConverterFragment extends Fragment {

  public static ConverterFragment newInstance(){
    return new ConverterFragment();
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.layout_converter, null);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    initResources();
    initUnitData();
    initControls(view);
    initChooseDialog();

    layout_root = view.findViewById(R.id.layout_root);
    layout_root.postDelayed(() -> {

      loadSettings();

      initLayout(view);
      setConverter(0);
      setCurrentInput(0);
      updateAllConverter();
    }, 200);

  }

  private AutoCalc autoCalc = new AutoCalc();

  private Resources resources;
  private int unit_text_orange;
  private int convert_unit_value;
  private String text_error;
  private String text_out_of_range;

  private LinearLayout layout_root;

  private TextView text_input_one;
  private TextView text_input_two;
  private Button btn_pad_number_negative;
  private Button btn_unit_choose_one;
  private Button btn_unit_choose_two;

  private ConverterItem currentInputItem = null;
  private List<ConverterItem> currentInputs = new ArrayList<>();
  private int currentSetUnitItemIndex = 0;

  //设置模式
  //=====================

  private void setConverterUnit(ConverterItem converterItem, int choosedIndex) {
    converterItem.updateUnitData(currentConvertGroup.getGroup().get(choosedIndex));
  }
  private void setConverter(int converter) {

    if(onModeChangedListener != null)
      onModeChangedListener.onModeChanged(converts_texts[converter]);

    currentConvertGroup = convertData.get(converter);
    if(currentConvertGroup.getGroup().size() > 2) {
      btn_unit_choose_one.setEnabled(true);
      btn_unit_choose_two.setEnabled(true);
    }else {
      btn_unit_choose_one.setEnabled(false);
      btn_unit_choose_two.setEnabled(false);
    }

    buildSelectUnitDialog();

    //Set default index
    for (int i1 = 0; i1 < currentInputs.size() && i1 < ConverterDataGroup.MAX_ONEPAGE_CONVERTER_COUNT; i1++) {
      ConverterItem i = currentInputs.get(i1);
      setConverterUnit(i, currentConvertGroup.getDefalutIndex()[i1]);
    }

    setCurrentInput(0);
    currentInputs.get(0).clearText();
    updateAllConverter();
  }
  private void setCurrentInput(int currentInput) {

    ConverterItem inputItem = currentInputs.get(currentInput);
    if(inputItem.getCanSelect()) {
      currentInputItem = inputItem;
      btn_pad_number_negative.setVisibility(inputItem.getCanBeNegative() ? View.VISIBLE : View.GONE);
      switch (currentInput) {
        case 0:
          text_input_two.setTextColor(convert_unit_value);
          text_input_one.setTextColor(unit_text_orange);
          break;
        case 1:
          text_input_two.setTextColor(unit_text_orange);
          text_input_one.setTextColor(convert_unit_value);
          break;
      }
    }
  }

  private void updateAllConverter() {
    BigDecimal benchmark;
    try {
      benchmark = currentInputItem.calculate();
      for (ConverterItem i: currentInputs) {
        if(i != currentInputItem) {
          i.fromBenchmark(benchmark);
          i.updateToView(true);
        }else {
          if(i.isValueOverflow()) i.updateToView(false, text_out_of_range);
          else i.updateToView(false);
        }
      }
    } catch (Exception e) {
      for (ConverterItem i: currentInputs) {
        if(i == currentInputItem) i.updateToView(false, text_error + " " +  e.getMessage());
        else i.forceUpdateToView( text_error + " " +  e.getMessage());
      }
    }

  }

  //输入文字
  //=====================

  private void inputText(String s) {
    currentInputItem.writeText(s);
    updateAllConverter();
  }
  private void delText() {
    currentInputItem.delText();
    updateAllConverter();
  }
  private void clearText() {
    currentInputItem.clearText();
    updateAllConverter();
  }

  //初始化界面
  //=====================

  private void initResources() {
    resources = getResources();
    unit_text_orange = resources.getColor(R.color.unit_text_orange, null);
    convert_unit_value = resources.getColor(R.color.convert_unit_value, null);
    text_error = resources.getString(R.string.text_error);
    text_out_of_range = resources.getString(R.string.text_out_of_range);
  }
  private void initControls(View view) {

    text_input_one = view.findViewById(R.id.text_input_one);
    text_input_two = view.findViewById(R.id.text_input_two);

    btn_unit_choose_one = view.findViewById(R.id.btn_unit_choose_one);
    btn_unit_choose_two = view.findViewById(R.id.btn_unit_choose_two);

    currentInputs.add(new ConverterItem(autoCalc, text_input_one, view.findViewById(R.id.text_unit_one), btn_unit_choose_one));
    currentInputs.add(new ConverterItem(autoCalc, text_input_two, view.findViewById(R.id.text_unit_two), btn_unit_choose_two));

    view.findViewById(R.id.view_input_one).setOnClickListener(v -> setCurrentInput(0));
    view.findViewById(R.id.view_input_two).setOnClickListener(v -> setCurrentInput(1));

    view.findViewById(R.id.btn_pad_ac).setOnClickListener(v -> clearText());
    view.findViewById(R.id.btn_pad_del).setOnClickListener(v -> delText());

    btn_pad_number_negative = view.findViewById(R.id.btn_pad_number_negative);

    btn_unit_choose_one.setOnClickListener(v -> {
      currentSetUnitItemIndex = 0;
      chooseUnitDialog.show();
    });
    btn_unit_choose_two.setOnClickListener(v -> {
      currentSetUnitItemIndex = 1;
      chooseUnitDialog.show();
    });


  }
  private void initLayout(View view) {

    LinearLayout view_pad = view.findViewById(R.id.view_pad);
    LinearLayout view_pad_right = view.findViewById(R.id.view_pad_right);

    layout_root.measure(0,0);

    int width = layout_root.getWidth();
    int height = layout_root.getHeight();

    int pad_height = (int)((double)height * 0.53);

    int btn_width = width / 4;
    int btn_height = pad_height / 4;

    ViewGroup.LayoutParams layoutParams = view_pad.getLayoutParams();
    layoutParams.height = pad_height;
    view_pad.setLayoutParams(layoutParams);

    TableLayout view_pad_left = view.findViewById(R.id.view_pad_left);
    Button btn;

    int butn_padding = PixelTool.dpToPx(getContext(), 6);
    for(int i = 0, c = view_pad_left.getChildCount(); i < c; i++) {
      TableRow row = (TableRow)view_pad_left.getChildAt(i);
      for(int j = 0, d = row.getChildCount(); j < d; j++) {
        btn = (Button)row.getChildAt(j);
        layoutParams = btn.getLayoutParams();
        layoutParams.height = btn_height;
        layoutParams.width = btn_width;
        btn.setPadding(butn_padding, butn_padding, butn_padding, butn_padding);
        btn.setLayoutParams(layoutParams);
        final String text = btn.getText().toString();
        btn.setOnClickListener((View v) -> inputText(text));
      }
    }

    layoutParams = view_pad_left.getLayoutParams();
    layoutParams.width = btn_width * 3;
    layoutParams.height = pad_height;
    view_pad_left.setLayoutParams(layoutParams);

    layoutParams = view_pad_right.getLayoutParams();
    layoutParams.height = pad_height;
    layoutParams.width = btn_width;
    view_pad_right.setLayoutParams(layoutParams);
  }

  private AlertDialog chooseConvertDialog;
  private AlertDialog chooseUnitDialog;

  public void showChooseConvertDialog() {
    chooseConvertDialog.show();
  }

  //ModeChanged
  //=====================

  public interface OnModeChangedListener {
    void onModeChanged(String newMdeString);
  }
  private OnModeChangedListener onModeChangedListener;
  public void setOnModeChangedListener(OnModeChangedListener listener) {
    onModeChangedListener = listener;
  }

  private TypedArray converts_icons;
  private String[] converts_texts;

  private void initChooseDialog() {
    final List<ConvertsListItem> functionsListItems = new ArrayList<>();
    final ConvertsListAdapter functionsListAdapter = new ConvertsListAdapter(getContext(), R.layout.item_convert, functionsListItems);

    LayoutInflater inflater = LayoutInflater.from(getContext());
    View v = inflater.inflate(R.layout.dialog_convert_list, null);

    chooseConvertDialog = AlertDialogTool.buildCustomStylePopupDialogGravity(getContext(), v, Gravity.TOP, R.style.DialogTopPopup);

    ListView list_all_converts = v.findViewById(R.id.list_all_converts);

    //初始化所有函数信息
    converts_texts = resources.getStringArray(R.array.converts_texts);
    converts_icons = getResources().obtainTypedArray(R.array.converts_icons);
    for (int i = 0, c = converts_texts.length; i < c; i++)
      functionsListItems.add(new ConvertsListItem(converts_texts[i],
              converts_icons.getDrawable(i)));

    functionsListAdapter.notifyDataSetChanged();

    list_all_converts.setAdapter(functionsListAdapter);
    list_all_converts.setOnItemClickListener((parent, view, position, id) -> {
      setConverter(position);
      chooseConvertDialog.dismiss();
    });
    v.findViewById(R.id.btn_cancel).setOnClickListener(view -> chooseConvertDialog.dismiss());
  }

  // 转换器数据
  //=====================

  private ConverterDataGroup currentConvertGroup = null;
  private int currentConvertBase = 0;

  private List<ConverterDataGroup> convertData = new ArrayList<>();

  private void initUnitData() {
    try {
      InputStream is = getActivity().getAssets().open("xml/converter_data.xml");
      XmlPullParser parser = Xml.newPullParser();
      parser.setInput(is, "utf-8");

      ConverterDataGroup group = null;
      ConverterData data = null;

      int type = parser.getEventType();
      while (type != XmlPullParser.END_DOCUMENT) {
        switch (type) {
          case XmlPullParser.START_TAG: {
            if ("group".equals(parser.getName())) {

              String name = parser.getAttributeValue(null, "name");
              group = new ConverterDataGroup(name);

              int attrCount = parser.getAttributeCount();
              for (int i = 1; i < attrCount; i++) {
                String attrName = parser.getAttributeName(i);
                String attrVal = parser.getAttributeValue(i);
                if ("base".equals(attrName))
                  group.setBaseIndex(Integer.parseInt(attrVal));
                else if (attrName.startsWith("def")) {
                  attrName = attrName.substring(3);
                  group.getDefalutIndex()[Integer.parseInt(attrName)-1] = Integer.parseInt(attrVal);
                }
              }

            }
            else if ("header".equals(parser.getName())) {
              data = new ConverterData(parser.nextText());
            }
            else if ("item".equals(parser.getName())) {
              String name = parser.getAttributeValue(null, "name");
              String shortName = parser.getAttributeValue(null, "shortName");
              data = new ConverterData(name, shortName);

              String selectable = parser.getAttributeValue(null, "selectable");
              if("false".equals(selectable))
                data.selectable = false;
              String stringConversion = parser.getAttributeValue(null, "stringConversion");
              if("true".equals(stringConversion))
                data.stringConversion = true;

            }
            else if (data != null) {
              if ("base".equals(parser.getName()))
                data.unitBase = Double.parseDouble(parser.nextText());
              else if ("calcType".equals(parser.getName()))
                data.calcType = Integer.parseInt(parser.nextText());
              else if ("minVal".equals(parser.getName()))
                data.setMinVal(Double.parseDouble(parser.nextText()));
              else if ("maxVal".equals(parser.getName()))
                data.setMaxVal(Double.parseDouble(parser.nextText()));
              else if ("toBenchmark".equals(parser.getName()))
                data.calcFormulaToBenchmark = parser.nextText();
              else if ("fromBenchmark".equals(parser.getName()))
                data.calcFormulaFromBenchmark = parser.nextText();
            }
            break;
          }
          case XmlPullParser.END_TAG: {
            if (group != null) {
              if ("item".equals(parser.getName()) || "header".equals(parser.getName()))
                group.add(data);
              else if ("group".equals(parser.getName()))
                convertData.add(group);
            }
            break;
          }
        }
        type = parser.next();
      }
    } catch (XmlPullParserException | IOException e) {
      e.printStackTrace();
    }
  }
  private void buildSelectUnitDialog() {

      final List<FunctionsListItem> functionsListItems = new ArrayList<>();
      final FunctionsListAdapter functionsListAdapter = new FunctionsListAdapter(getContext(), R.layout.item_function, functionsListItems);

      LayoutInflater inflater = LayoutInflater.from(getContext());
      View v = inflater.inflate(R.layout.dialog_funs_list, null);

      chooseUnitDialog = AlertDialogTool.buildCustomBottomPopupDialog(getContext(), v);

      ListView list_all_functions = v.findViewById(R.id.list_all_functions);

      for (ConverterData value : currentConvertGroup.getGroup()) {
        if(value.isTitle) functionsListItems.add(new FunctionsListItem(value.unitName));
        else functionsListItems.add(new FunctionsListItem(value.unitName, value.unitNameShort));
      }

      list_all_functions.setAdapter(functionsListAdapter);
      list_all_functions.setOnItemClickListener((parent, view, position, id) -> {
        ConverterData data = currentConvertGroup.getGroup().get(position);
        if(!data.isTitle) {
          setConverterUnit(currentInputs.get(currentSetUnitItemIndex), position);
          chooseUnitDialog.dismiss();
          updateAllConverter();
        }
      });

      functionsListAdapter.notifyDataSetChanged();

      ((TextView) v.findViewById(R.id.text_title)).setText(resources.getString(R.string.text_choose_unit));
      v.findViewById(R.id.text_sub_title).setVisibility(View.GONE);
      v.findViewById(R.id.btn_cancel).setOnClickListener(view -> chooseUnitDialog.dismiss());

  }

  //设置
  //=====================

  private void loadSettings() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
    autoCalc.setRecordStep(prefs.getBoolean("calc_record_step", false));
    autoCalc.setNumberScale(prefs.getInt("calc_computation_accuracy", 8));
    autoCalc.setUseDegree(prefs.getBoolean("calc_use_deg", true));
    autoCalc.setAutoScientificNotation(prefs.getBoolean("calc_auto_scientific_notation", true));
    autoCalc.setScientificNotationMax(prefs.getInt("calc_scientific_notation_max", 100000));
  }
  public void updateSettings() {
    loadSettings();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    converts_icons.recycle();
  }

}
