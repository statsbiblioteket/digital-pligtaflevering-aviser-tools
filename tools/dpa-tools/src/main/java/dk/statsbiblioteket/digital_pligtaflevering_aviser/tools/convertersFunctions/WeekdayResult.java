package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import java.util.List;

public class WeekdayResult {
    
    
                    /*  Now generate this snippet:

                    {
                    "weekday": "Mon",
                    "expectedTitles": ["avis1","avis2"],
                    "foundTitles": ["avis1","avis3"],
                    "missingTitles":["avis2"],
                    "extraTitles":["avis3"],
                    }
                     */
    
    private String weekday;
    private List<String> expectedTitles, foundTitles, missingTitles, extraTitles;
    
    public WeekdayResult(String weekday,
                         List<String> expectedTitles,
                         List<String> foundTitles,
                         List<String> missingTitles, List<String> extraTitles) {
        this.weekday = weekday;
        this.expectedTitles = expectedTitles;
        this.foundTitles = foundTitles;
        this.missingTitles = missingTitles;
        this.extraTitles = extraTitles;
    }
    
    public String getWeekday() {
        return weekday;
    }
    
    public List<String> getExpectedTitles() {
        return expectedTitles;
    }
    
    public List<String> getFoundTitles() {
        return foundTitles;
    }
    
    public List<String> getMissingTitles() {
        return missingTitles;
    }
    
    public List<String> getExtraTitles() {
        return extraTitles;
    }
}
