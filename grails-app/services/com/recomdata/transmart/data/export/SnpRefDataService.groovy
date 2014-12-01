package com.recomdata.transmart.data.export;

import de.DeSNPInfo;

class SnpRefDataService {

    boolean transactional = false

    def Collection<String> findRsIdByGeneNames(Collection geneNames) {
        return DeSNPInfo.executeQuery("SELECT distinct rsId FROM DeSNPInfo s WHERE s.geneName IN (:gn)", [gn: geneNames]);

    }

    def findRefByRsId(String rsid) {
        if (rsid != null) {
            def nrsid = rsid.trim().toLowerCase();
            if (!nrsid.startsWith("rs"))
                nrsid = "rs" + nrsid;
            return DeSNPInfo.findByRsId(nrsid);
        }
        return null;

    }


}