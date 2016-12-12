<table width="100%">
    <tbody>
    <tr>
        <td align="center">
            <div class="smalltitle">
                <b>Subject Totals</b>
            </div>
            <table class="analysis">
                <tbody>
                <tr>
                    <th>Subset 1</th>
                    <th>Both</th>
                    <th>Subset 2</th>
                </tr>
                <tr>
                    <td>${subsets[1]?.patientCount?:0}</td>
                    <td>${subsets?.commons?.patientIntersectionCount?:0}</td>
                    <td>${subsets[2]?.patientCount?:0}</td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    </tbody>
</table>