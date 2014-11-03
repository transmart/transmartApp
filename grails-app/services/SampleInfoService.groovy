import i2b2.SampleInfo;

class SampleInfoService {
    def dataSource;

    /*
     * Get a list of SampleInfo objects based on a string of sample ID's.
     */

    public List<SampleInfo> getSampleInfoListInOrder(String sampleIdListStr) throws Exception {
        //Verify we received a sample id list.
        if (sampleIdListStr == null || sampleIdListStr.length() == 0) return null;

        //Get the list of SampleInfo objects from de_subject_sample_mapping.
        List<SampleInfo> sampleInfoList = SampleInfo.findAll("from SampleInfo where id in (" + quoteCSV(sampleIdListStr) + ")");

        //Create a map of the SampleInfo objects with their id as the key.
        Map<Long, SampleInfo> sampleInfoMap = new HashMap<Long, SampleInfo>();
        for (SampleInfo sampleInfo : sampleInfoList) {
            sampleInfoMap.put(sampleInfo.id, sampleInfo);
        }

        //Construct a list of sampleID's in the same order as the passed in string.
        List<SampleInfo> sampleInfoListInOrder = new ArrayList<SampleInfo>();

        //Split the input string.
        String[] sampleIdStrList = sampleIdListStr.split(",");

        //For each id in the string add that sampleId to the list.
        for (String sampleIdStr : sampleIdStrList) {
            //Add the sampleID to the list.
            sampleInfoListInOrder.add(sampleInfoMap.get(sampleIdStr));

        }
        return sampleInfoListInOrder;
    }

    /*
     * Take a comma seperated list and return the same list with single quotes around each item.
     */

    def String quoteCSV(String val) {
        String[] inArray;
        StringBuilder s = new StringBuilder();

        if (val != null && val.length() > 0) {
            inArray = val.split(",");
            s.append("'" + inArray[0] + "'");
            for (int i = 1; i < inArray.length; i++) {
                s.append(",'" + inArray[i] + "'");
            }
        }
        return s.toString();
    }

}
