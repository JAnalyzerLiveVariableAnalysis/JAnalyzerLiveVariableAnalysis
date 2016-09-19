package softwareMeasurement.metric;

import java.util.ArrayList;
import java.util.List;

import softwareMeasurement.measure.SoftwareMeasure;
import softwareMeasurement.measure.SoftwareMeasureIdentifier;
import softwareMeasurement.metric.cohesion.CAMCMetric;
import softwareMeasurement.metric.cohesion.CCMetric;
//import softwareMeasurement.metric.cohesion.CoMetric;
//import softwareMeasurement.metric.cohesion.CoPrimMetric;
import softwareMeasurement.metric.cohesion.CohMetric;
import softwareMeasurement.metric.cohesion.DCDMetric;
import softwareMeasurement.metric.cohesion.DCIMetric;
import softwareMeasurement.metric.cohesion.ICHMetric;
import softwareMeasurement.metric.cohesion.LCCMetric;
import softwareMeasurement.metric.cohesion.LCOM125Metric;
import softwareMeasurement.metric.cohesion.LCOM34Metric;
//import softwareMeasurement.metric.cohesion.LCOM3Metric;
//import softwareMeasurement.metric.cohesion.LCOM4Metric;
import softwareMeasurement.metric.cohesion.LSCCMetric;
import softwareMeasurement.metric.cohesion.NHDMetric;
//import softwareMeasurement.metric.cohesion.OCCMetric;
import softwareMeasurement.metric.cohesion.SCCMetric;
import softwareMeasurement.metric.cohesion.SCOMMetric;
import softwareMeasurement.metric.cohesion.TCCMetric;
import softwareMeasurement.metric.coupling.ACAICMetric;
import softwareMeasurement.metric.coupling.ACMICMetric;
import softwareMeasurement.metric.coupling.AMMICMetric;
import softwareMeasurement.metric.coupling.CBOMetric;
import softwareMeasurement.metric.coupling.DACMetric;
import softwareMeasurement.metric.coupling.DACpMetric;
import softwareMeasurement.metric.coupling.DCAECMetric;
import softwareMeasurement.metric.coupling.DCMECMetric;
import softwareMeasurement.metric.coupling.DMMECMetric;
import softwareMeasurement.metric.coupling.ICPMetric;
import softwareMeasurement.metric.coupling.IHICPMetric;
import softwareMeasurement.metric.coupling.MPCMetric;
import softwareMeasurement.metric.coupling.OCAECMetric;
import softwareMeasurement.metric.coupling.OCAICMetric;
import softwareMeasurement.metric.coupling.OCMECMetric;
import softwareMeasurement.metric.coupling.OCMICMetric;
import softwareMeasurement.metric.coupling.OMMECMetric;
import softwareMeasurement.metric.coupling.OMMICMetric;
import softwareMeasurement.metric.coupling.RFCMetric;
import softwareMeasurement.metric.coupling.RFCpMetric;
import softwareMeasurement.metric.size.TypeCounterMetric;
import softwareMeasurement.metric.size.CodeLineCounterMetric;
import softwareMeasurement.metric.size.DefinitionCounterMetric;
import softwareMeasurement.metric.size.FieldCounterMetric;
import softwareMeasurement.metric.size.MethodCounterMetric;
import softwareMeasurement.metric.size.StatementCounterMetric;
import softwareMeasurement.metric.inheritance.DITMetric;
import softwareMeasurement.metric.inheritance.NOCMetric;
import softwareMeasurement.metric.inheritance.NOAMetric;
import softwareMeasurement.metric.inheritance.NODMetric;
import softwareMeasurement.metric.inheritance.NOPMetric;
import softwareMeasurement.metric.inheritance.AIDMetric;
import softwareMeasurement.metric.inheritance.CLDMetric;
import softwareMeasurement.metric.inheritance.SIXMetric;
import softwareMeasurement.metric.inheritance.DPAMetric;
import softwareMeasurement.metric.inheritance.DPDMetric;
import softwareMeasurement.metric.inheritance.DPMetric;
import softwareMeasurement.metric.inheritance.SPAMetric;
import softwareMeasurement.metric.inheritance.SPDMetric;
import softwareMeasurement.metric.inheritance.SPMetric;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê7ÔÂ2ÈÕ
 * @version 1.0
 */
public class SoftwareStructMetricFactory {
	private static final SoftwareStructMetric LCOM125 = new LCOM125Metric();
	private static final SoftwareStructMetric LCOM34 = new LCOM34Metric();
	private static final SoftwareStructMetric Coh = new CohMetric();
	private static final SoftwareStructMetric TCC = new TCCMetric();
	private static final SoftwareStructMetric LCC = new LCCMetric();
	private static final SoftwareStructMetric DCD = new DCDMetric();
	private static final SoftwareStructMetric DCI = new DCIMetric();
//	private static final SoftwareStructMetric OCC = new OCCMetric();
	private static final SoftwareStructMetric CC = new CCMetric();
	private static final SoftwareStructMetric SCOM = new SCOMMetric();
	private static final SoftwareStructMetric LSCC = new LSCCMetric();
	private static final SoftwareStructMetric CAMC = new CAMCMetric();
	private static final SoftwareStructMetric ICH = new ICHMetric();
	private static final SoftwareStructMetric NHD = new NHDMetric();
	private static final SoftwareStructMetric SCC = new SCCMetric();

	private static final SoftwareStructMetric CBO = new CBOMetric();
	private static final SoftwareStructMetric RFC = new RFCMetric();
	private static final SoftwareStructMetric RFCp = new RFCpMetric();
	private static final SoftwareStructMetric MPC = new MPCMetric();
	private static final SoftwareStructMetric DAC = new DACMetric();
	private static final SoftwareStructMetric DACp = new DACpMetric();
	private static final SoftwareStructMetric ICP = new ICPMetric();
	private static final SoftwareStructMetric IHICP = new IHICPMetric();
	private static final SoftwareStructMetric ACAIC = new ACAICMetric();
	private static final SoftwareStructMetric OCAIC = new OCAICMetric();
	private static final SoftwareStructMetric DCAEC = new DCAECMetric();
	private static final SoftwareStructMetric OCAEC = new OCAECMetric();
	private static final SoftwareStructMetric ACMIC = new ACMICMetric();
	private static final SoftwareStructMetric OCMIC = new OCMICMetric();
	private static final SoftwareStructMetric DCMEC = new DCMECMetric();
	private static final SoftwareStructMetric OCMEC = new OCMECMetric();
	private static final SoftwareStructMetric AMMIC = new AMMICMetric();
	private static final SoftwareStructMetric OMMIC = new OMMICMetric();
	private static final SoftwareStructMetric DMMEC = new DMMECMetric();
	private static final SoftwareStructMetric OMMEC = new OMMECMetric();

	private static final SoftwareStructMetric definitionCounter = new DefinitionCounterMetric();
	private static final SoftwareStructMetric statementCounter = new StatementCounterMetric();
	private static final SoftwareStructMetric lineCounter = new CodeLineCounterMetric();
	private static final SoftwareStructMetric typeCounter = new TypeCounterMetric();
	private static final SoftwareStructMetric fieldCounter = new FieldCounterMetric();
	private static final SoftwareStructMetric methodCounter = new MethodCounterMetric();
	
	private static final SoftwareStructMetric DIT = new DITMetric();
	private static final SoftwareStructMetric NOC = new NOCMetric();
	private static final SoftwareStructMetric NOA = new NOAMetric();
	private static final SoftwareStructMetric NOD = new NODMetric();
	private static final SoftwareStructMetric NOP = new NOPMetric();
	private static final SoftwareStructMetric AID = new AIDMetric();
	private static final SoftwareStructMetric CLD = new CLDMetric();
	private static final SoftwareStructMetric SIX = new SIXMetric();
	private static final SoftwareStructMetric DPA = new DPAMetric();
	private static final SoftwareStructMetric DPD = new DPDMetric();
	private static final SoftwareStructMetric DP = new DPMetric();
	private static final SoftwareStructMetric SPA = new SPAMetric();
	private static final SoftwareStructMetric SPD = new SPDMetric();
	private static final SoftwareStructMetric SP = new SPMetric();
	
	protected static SoftwareMeasureMetricMatch[] table = {
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.FILE, definitionCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.PKG, definitionCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.CLS, definitionCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.INTF, definitionCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.ENUM, definitionCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.FLD, definitionCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.MTHD, definitionCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.PARS, definitionCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LOCV, definitionCounter), 

		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.STMN, statementCounter), 

		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.BLOC, lineCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.BYTE, lineCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.CLOC, lineCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.ELOC, lineCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LOPT, lineCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LOC, lineCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.NLOC, lineCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.WORD, lineCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.CHAR, lineCounter), 
		
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.NonTopTYPE, typeCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.TopPubTYPE, typeCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.TopNonPubTYPE, typeCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.NonTopCLS, typeCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.TopPubCLS, typeCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.TopNonPubCLS, typeCounter), 

		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.ALLMTHD, methodCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.IHMTHD, methodCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.NEWMTHD, methodCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.OVMTHD, methodCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.IMPMTHD, methodCounter), 

		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.ALLFLD, fieldCounter), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.IHFLD, fieldCounter), 
		
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LCOM1, LCOM125), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LCOM2, LCOM125),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LCOM1p, LCOM125), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LCOM2p, LCOM125),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LCOM5, LCOM125),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LCOM3, LCOM34),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LCOM4, LCOM34),
//		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.Co, Co),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.CoPrim, LCOM34),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.Coh, Coh),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.TCC, TCC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.TCCp, TCC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LCC, LCC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LCCp, LCC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DCD, DCD),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DCDp, DCD),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DCI, DCI),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DCIp, DCI),
//		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.OCC, OCC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.CC, CC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.CCp, CC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.SCOM, SCOM),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.SCOMp, SCOM),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.LSCC, LSCC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.ICH, ICH),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.CAMC, CAMC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.NHD, NHD),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.SCC, SCC),

		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.CBO,CBO),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.CBOi,CBO),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.CBOe,CBO),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.CBOp,CBO),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.RFC,RFC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.RFCp,RFCp),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.MPC,MPC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DAC,DAC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DACp,DACp),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.ICP,ICP),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.IHICP,IHICP),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.ACAIC,ACAIC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.OCAIC,OCAIC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DCAEC,DCAEC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.OCAEC,OCAEC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.ACMIC,ACMIC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.OCMIC,OCMIC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DCMEC,DCMEC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.OCMEC,OCMEC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.AMMIC,AMMIC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.OMMIC,OMMIC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DMMEC,DMMEC),
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.OMMEC,OMMEC),
		
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DIT, DIT), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.NOC, NOC), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.NOD, NOD), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.NOA, NOA), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.NOP, NOP), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.AID, AID), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.CLD, CLD), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.SIX, SIX), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DPA, DPA), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DPD, DPD), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.DP, DP), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.SPA, SPA), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.SPD, SPD), 
		new SoftwareMeasureMetricMatch(SoftwareMeasureIdentifier.SP, SP), 
	};

	
	protected static List<SoftwareMeasureMetricMatch> extendedTable = new ArrayList<SoftwareMeasureMetricMatch>();
	
	public static SoftwareStructMetric getMetricInstance(String measureIdentifier) {
		for (int index = extendedTable.size()-1; index >= 0; index--) {
			SoftwareMeasureMetricMatch match = extendedTable.get(index);
			if (match.getIdentifier().equals(measureIdentifier)) return match.getMetric();
		}
		for (int index = table.length-1; index >= 0; index--) {
			if (table[index].getIdentifier().equals(measureIdentifier)) return table[index].getMetric();
		}
		
		throw new AssertionError("Can not find metric for measure [" + measureIdentifier + "]!");
	}
	
	public static SoftwareStructMetric getMetricInstance(SoftwareMeasure measure) {
		return getMetricInstance(measure.getIdentifier());
	}
	
	public static void registerMetric(String measureIdentifier, SoftwareStructMetric metric) {
		extendedTable.add(new SoftwareMeasureMetricMatch(measureIdentifier, metric));
	}

	public static void registerMetric(SoftwareMeasure measure, SoftwareStructMetric metric) {
		extendedTable.add(new SoftwareMeasureMetricMatch(measure.getIdentifier(), metric));
	}
}

class SoftwareMeasureMetricMatch {
	private String measureIdentifier;
	private SoftwareStructMetric metric;
	
	public SoftwareMeasureMetricMatch(String identifier, SoftwareStructMetric metric) {
		this.measureIdentifier = identifier;
		this.metric = metric;
	}
	
	public String getIdentifier() {
		return measureIdentifier;
	}
	
	public SoftwareStructMetric getMetric() {
		return metric;
	}
}
