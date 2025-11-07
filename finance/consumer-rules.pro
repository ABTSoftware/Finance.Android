# keep @EditableProperty methods
-keepclassmembers class ** {
  @com.scitrader.finance.edit.annotations.EditableProperty public *;
}
-keepnames class com.scitrader.finance.indicators.* { *; }