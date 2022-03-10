package com.scitrader.finance.indicators

import com.scichart.core.framework.IAttachable
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.state.IEditablePropertyContainer

interface IIndicator : IAttachable, IEditable, IEditablePropertyContainer{
}
