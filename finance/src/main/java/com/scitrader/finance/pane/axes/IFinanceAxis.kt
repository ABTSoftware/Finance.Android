package com.scitrader.finance.pane.axes

import com.scichart.core.framework.IAttachable
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.pane.IPanePlaceable
import com.scitrader.finance.state.IEditablePropertyContainer

interface IFinanceAxis : IAttachable, IEditable, IPanePlaceable, IEditablePropertyContainer {
    var axisId: AxisId
}
