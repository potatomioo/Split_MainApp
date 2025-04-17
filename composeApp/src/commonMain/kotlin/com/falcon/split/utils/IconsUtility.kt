import com.falcon.split.data.network.models_app.ExpenseType
import com.falcon.split.data.network.models_app.GroupType
import org.jetbrains.compose.resources.DrawableResource
import split.composeapp.generated.resources.ApproveSettlement
import split.composeapp.generated.resources.DeclineSettlement
import split.composeapp.generated.resources.Group_Entertainment
import split.composeapp.generated.resources.Group_Home
import split.composeapp.generated.resources.Group_Office
import split.composeapp.generated.resources.Group_Others
import split.composeapp.generated.resources.Group_Sports
import split.composeapp.generated.resources.Group_trip
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.Settlement
import split.composeapp.generated.resources.expense_bills
import split.composeapp.generated.resources.expense_entertainment
import split.composeapp.generated.resources.expense_food
import split.composeapp.generated.resources.expense_groceries
import split.composeapp.generated.resources.expense_others
import split.composeapp.generated.resources.expense_shopping
import split.composeapp.generated.resources.expense_stay
import split.composeapp.generated.resources.expense_travel

fun getGroupIconByType(groupType: String?): DrawableResource {
    return when (GroupType.fromString(groupType)) {
        GroupType.HOME -> Res.drawable.Group_Home
        GroupType.TRIP -> Res.drawable.Group_trip
        GroupType.OFFICE -> Res.drawable.Group_Office
        GroupType.SPORTS -> Res.drawable.Group_Sports
        GroupType.ENTERTAINMENT -> Res.drawable.Group_Entertainment
        GroupType.OTHER -> Res.drawable.Group_Others
    }
}

fun getExpenseIconByType(expenseType: String?): DrawableResource {
    return when (ExpenseType.fromString(expenseType)) {
        ExpenseType.TRAVEL -> Res.drawable.expense_travel
        ExpenseType.STAY -> Res.drawable.expense_stay
        ExpenseType.SHOPPING -> Res.drawable.expense_shopping
        ExpenseType.GROCERIES -> Res.drawable.expense_groceries
        ExpenseType.FOOD -> Res.drawable.expense_food
        ExpenseType.BILLS -> Res.drawable.expense_bills
        ExpenseType.ENTERTAINMENT -> Res.drawable.expense_entertainment
        ExpenseType.OTHER -> Res.drawable.expense_others
    }
}

val settlement_requested = Res.drawable.Settlement
val settlement_approved = Res.drawable.ApproveSettlement
val settlement_declined = Res.drawable.DeclineSettlement