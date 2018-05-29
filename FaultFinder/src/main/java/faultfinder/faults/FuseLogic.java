package faultfinder.faults;

import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;

import faultfinder.objects.StatusChangeDB;
import faultfinder.service.MainFrameService;
import faultfinder.utils.MainFrameServiceManager;
import faultfinder.utils.StringConstants;

public class FuseLogic implements FaultLogic {
	private MainFrameService mainFrameService = MainFrameServiceManager.getSession();
	private Map<Integer, Pair<Integer, Integer>> aMap;

	@Override
	public void drawLogic(EmbeddedCanvas canvas, H2F mouseH2F, int xBin, int yBin) {
		this.aMap = FuseBundles.findWireRange(xBin, yBin);
		for (int i = 0; i < mouseH2F.getXAxis().getNBins(); i++) {
			for (int j = 0; j < mouseH2F.getYAxis().getNBins(); j++) {
				mouseH2F.setBinContent(i, j, canvas.getPad().getDatasetPlotters().get(0).getDataSet().getData(i, j));
			}
		}

		for (int j = 0; j < mouseH2F.getYAxis().getNBins(); j++) {
			Pair<Integer, Integer> xPair = aMap.get(j + 1);
			for (int i = xPair.getLeft(); i <= xPair.getRight(); i++) {
				mouseH2F.setBinContent(i - 1, j, 0.0);
			}

		}

		canvas.draw(mouseH2F, "same");
		canvas.update();
	}

	@Override
	public void setFaultToDB() {
		TreeSet<StatusChangeDB> queryList = new TreeSet<>();

		for (int j = 0; j < 6; j++) {
			Pair<Integer, Integer> xPair = aMap.get(j + 1);
			for (int i = xPair.getLeft(); i <= xPair.getRight(); i++) {
				StatusChangeDB statusChangeDB = new StatusChangeDB();
				statusChangeDB.setSector(String.valueOf(this.mainFrameService.getSelectedSector()));
				statusChangeDB.setSuperlayer(String.valueOf(this.mainFrameService.getSelectedSuperlayer()));
				statusChangeDB.setLoclayer(String.valueOf(j + 1));
				statusChangeDB.setLocwire(String.valueOf(i));
				statusChangeDB.setProblem_type(StringConstants.PROBLEM_TYPES[this.mainFrameService.getFaultNum() + 1]);
				statusChangeDB.setStatus_change_type(this.mainFrameService.getBrokenOrFixed().toString());
				statusChangeDB.setRunno(this.mainFrameService.getRunNumber());
				queryList.add(statusChangeDB);
			}

		}
		this.mainFrameService.prepareMYSQLQuery(queryList);
		this.mainFrameService.addToCompleteSQLList(queryList);
		this.mainFrameService.getDataPanel().removeItems(queryList);
		this.mainFrameService.clearTempSQLList();
		this.mainFrameService.getSQLPanel().setTableModel(this.mainFrameService.getCompleteSQLList());

	}

	@Override
	public int setBundle(int xBin, int yBin) {
		return FuseBundles.getBundle(xBin, yBin);
	}

}
