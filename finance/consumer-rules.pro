# keep @EditableProperty methods
-keepclassmembers class ** {
  @com.scitrader.finance.edit.annotations.EditableProperty public *;
}
