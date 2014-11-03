import i2b2.Concept

class ConceptService {
    def dataSource;

    public Concept getConceptByBaseCode(String baseCode) throws Exception {
        return Concept.findByBaseCode(baseCode);
    }

    public List<Concept> getChildrenConcepts(Concept concept) throws Exception {
        if (concept == null || concept.id == null || concept.getLevel() == null) return null;
        List<Concept> conceptList =
                Concept.findAll("from Concept as c where c.fullName like :fullNameLike and level = :levelNew",
                        [fullNameLike: concept.getFullName() + "%", levelNew: concept.getLevel().intValue() + 1]);
        return conceptList;
    }

}
