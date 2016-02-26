package roth.lib.java.test.table;

import java.io.Serializable;

import roth.lib.java.annotation.Entity;
import roth.lib.java.annotation.Property;
import roth.lib.java.deserializer.IntegerCurrencyDeserializer;
import roth.lib.java.filter.NumberFilterer;
import roth.lib.java.filter.TrimFilterer;
import roth.lib.java.json.JsonMapper;
import roth.lib.java.time.Time;

@Entity
@SuppressWarnings("serial")
public class TableModel implements Serializable
{
	@Property(name = "transactionTime", tableName = "TransactionDateTime", tableTimeFormat = "M/d/yyy h:mm:ss a")
	protected Time transactionTime;
	
	@Property(name = "accountNum", tableName = "account_num")
	protected String accountNum;
	
	@Property(name = "transId", tableName = "Trans_Id")
	protected String transId;
	
	@Property(name = "attNum", tableName = "att_num")
	protected String attNum;
	
	@Property(name = "transactionType", tableName = "TransactionType")
	protected String transactionType;
	
	@Property(name = "amount", tableName = "Amount", tableFilter = NumberFilterer.class, tableDeserializer = IntegerCurrencyDeserializer.class)
	protected Integer amount;
	
	@Property(name = "netAmount", tableName = "NetAmount", tableFilter = NumberFilterer.class, tableDeserializer = IntegerCurrencyDeserializer.class)
	protected Integer netAmount;
	
	@Property(name = "invNum", tableName = "inv_num", tableFilter = TrimFilterer.class)
	protected String invNum;
	
	@Property(name = "details", tableName = "Details")
	protected String details;
	
	@Property(name = "achReturnCode", tableName = "AchReturnCode")
	protected String achReturnCode;

	@Property(name = "returnCodeDescription", tableName = "ReturnCodeDescription")
	protected String returnCodeDescription;
	
	@Property(name = "chargeBackResponeCode", tableName = "ChargeBackResponeCode")
	protected String chargeBackResponeCode;
	
	@Property(name = "responeCodeDescription", tableName = "ResponeCodeDescription")
	protected String responeCodeDescription;
	
	public TableModel()
	{
		
	}
	
	public TableModel(Time transactionTime, String accountNum, String transId, String attNum, String transactionType, Integer amount, Integer netAmount, String invNum, String details, String achReturnCode, String returnCodeDescription, String chargeBackResponeCode, String responeCodeDescription)
	{
		this.transactionTime = transactionTime;
		this.accountNum = accountNum;
		this.transId = transId;
		this.attNum = attNum;
		this.transactionType = transactionType;
		this.amount = amount;
		this.netAmount = netAmount;
		this.invNum = invNum;
		this.details = details;
		this.achReturnCode = achReturnCode;
		this.returnCodeDescription = returnCodeDescription;
		this.chargeBackResponeCode = chargeBackResponeCode;
		this.responeCodeDescription = responeCodeDescription;
	}
	
	public Time getTransactionTime()
	{
		return transactionTime;
	}
	
	public String getAccountNum()
	{
		return accountNum;
	}
	
	public String getTransId()
	{
		return transId;
	}
	
	public String getAttNum()
	{
		return attNum;
	}
	
	public String getTransactionType()
	{
		return transactionType;
	}
	
	public Integer getAmount()
	{
		return amount;
	}
	
	public Integer getNetAmount()
	{
		return netAmount;
	}
	
	public String getInvNum()
	{
		return invNum;
	}
	
	public String getDetails()
	{
		return details;
	}
	
	public String getAchReturnCode()
	{
		return achReturnCode;
	}
	
	public String getReturnCodeDescription()
	{
		return returnCodeDescription;
	}
	
	public String getChargeBackResponeCode()
	{
		return chargeBackResponeCode;
	}
	
	public String getResponeCodeDescription()
	{
		return responeCodeDescription;
	}
	
	public TableModel setTransactionTime(Time transactionTime)
	{
		this.transactionTime = transactionTime;
		return this;
	}
	
	public TableModel setAccountNum(String accountNum)
	{
		this.accountNum = accountNum;
		return this;
	}
	
	public TableModel setTransId(String transId)
	{
		this.transId = transId;
		return this;
	}
	
	public TableModel setAttNum(String attNum)
	{
		this.attNum = attNum;
		return this;
	}
	
	public TableModel setTransactionType(String transactionType)
	{
		this.transactionType = transactionType;
		return this;
	}
	
	public TableModel setAmount(Integer amount)
	{
		this.amount = amount;
		return this;
	}
	
	public TableModel setNetAmount(Integer netAmount)
	{
		this.netAmount = netAmount;
		return this;
	}
	
	public TableModel setInvNum(String invNum)
	{
		this.invNum = invNum;
		return this;
	}
	
	public TableModel setDetails(String details)
	{
		this.details = details;
		return this;
	}
	
	public TableModel setAchReturnCode(String achReturnCode)
	{
		this.achReturnCode = achReturnCode;
		return this;
	}
	
	public TableModel setReturnCodeDescription(String returnCodeDescription)
	{
		this.returnCodeDescription = returnCodeDescription;
		return this;
	}
	
	public TableModel setChargeBackResponeCode(String chargeBackResponeCode)
	{
		this.chargeBackResponeCode = chargeBackResponeCode;
		return this;
	}
	
	public TableModel setResponeCodeDescription(String responeCodeDescription)
	{
		this.responeCodeDescription = responeCodeDescription;
		return this;
	}
	
	@Override
	public String toString()
	{
		return new JsonMapper().setPrettyPrint(true).serialize(this);
	}
	
}
