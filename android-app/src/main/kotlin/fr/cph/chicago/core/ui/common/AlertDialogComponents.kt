package fr.cph.chicago.core.ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import fr.cph.chicago.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LicenseDialog(
    show: Boolean,
    hideDialog: () -> Unit
) {
    if (show) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 10.dp),
            onDismissRequest = hideDialog,
            // FIXME workaround because the dialog do not resize after loading. Issue: https://issuetracker.google.com/issues/194911971?pli=1
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = {
                TitleDetailDialog(title = "Apache License, Version 2.0")
            },
            text = {
                Column(modifier = Modifier.verticalScroll(ScrollState(0))) {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = ParagraphStyle(textAlign = TextAlign.Center)) {
                                append("Apache License\n")
                                append("Version 2.0, January 2004\n")
                                append("http://www.apache.org/licenses/\n\n")
                            }
                            withStyle(style = ParagraphStyle(textAlign = TextAlign.Left)) {
                                append("TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION\n\n")
                                append("1. Definitions.\n\n")
                                append(
                                    "\"License\" shall mean the terms and conditions for use, reproduction, " +
                                        "and distribution as defined by Sections 1 through 9 of this document.\n\n"
                                )
                                append(
                                    "\"Licensor\" shall mean the copyright owner or entity authorized by" +
                                        " the copyright owner that is granting the License.\n\n"
                                )
                                append(
                                    "\"Legal Entity\" shall mean the union of the acting entity and all" +
                                        " other entities that control, are controlled by, or are under common" +
                                        " control with that entity. For the purposes of this definition," +
                                        " \"control\" means (i) the power, direct or indirect, to cause the" +
                                        " direction or management of such entity, whether by contract or" +
                                        " otherwise, or (ii) ownership of fifty percent (50%) or more of the" +
                                        " outstanding shares, or (iii) beneficial ownership of such entity.\n\n"
                                )
                                append(
                                    "\"You\" (or \"Your\") shall mean an individual or Legal Entity" +
                                        " exercising permissions granted by this License.\n\n" +
                                        " \"Source\" form shall mean the preferred form for making modifications," +
                                        " including but not limited to software source code, documentation" +
                                        " source, and configuration files.\n\n" +
                                        " \"Object\" form shall mean any form resulting from mechanical" +
                                        " transformation or translation of a Source form, including but" +
                                        " not limited to compiled object code, generated documentation," +
                                        " and conversions to other media types.\n" +
                                        "\n" +
                                        " \"Work\" shall mean the work of authorship, whether in Source or" +
                                        " Object form, made available under the License, as indicated by a" +
                                        " copyright notice that is included in or attached to the work" +
                                        " (an example is provided in the Appendix below).\n" +
                                        "\n" +
                                        " \"Derivative Works\" shall mean any work, whether in Source or Object" +
                                        " form, that is based on (or derived from) the Work and for which the" +
                                        " editorial revisions, annotations, elaborations, or other modifications" +
                                        " represent, as a whole, an original work of authorship. For the purposes" +
                                        " of this License, Derivative Works shall not include works that remain" +
                                        " separable from, or merely link (or bind by name) to the interfaces of," +
                                        " the Work and Derivative Works thereof.\n" +
                                        "\n" +
                                        " \"Contribution\" shall mean any work of authorship, including" +
                                        " the original version of the Work and any modifications or additions" +
                                        " to that Work or Derivative Works thereof, that is intentionally" +
                                        " submitted to Licensor for inclusion in the Work by the copyright owner" +
                                        " or by an individual or Legal Entity authorized to submit on behalf of" +
                                        " the copyright owner. For the purposes of this definition, \"submitted\"" +
                                        " means any form of electronic, verbal, or written communication sent" +
                                        " to the Licensor or its representatives, including but not limited to" +
                                        " communication on electronic mailing lists, source code control systems," +
                                        " and issue tracking systems that are managed by, or on behalf of, the" +
                                        " Licensor for the purpose of discussing and improving the Work, but" +
                                        " excluding communication that is conspicuously marked or otherwise" +
                                        " designated in writing by the copyright owner as \"Not a Contribution.\"\n" +
                                        "\n" +
                                        " \"Contributor\" shall mean Licensor and any individual or Legal Entity" +
                                        " on behalf of whom a Contribution has been received by Licensor and" +
                                        " subsequently incorporated within the Work.\n" +
                                        "\n" +
                                        " 2. Grant of Copyright License. Subject to the terms and conditions of" +
                                        " this License, each Contributor hereby grants to You a perpetual," +
                                        " worldwide, non-exclusive, no-charge, royalty-free, irrevocable" +
                                        " copyright license to reproduce, prepare Derivative Works of," +
                                        " publicly display, publicly perform, sublicense, and distribute the" +
                                        " Work and such Derivative Works in Source or Object form.\n" +
                                        "\n" +
                                        " 3. Grant of Patent License. Subject to the terms and conditions of" +
                                        " this License, each Contributor hereby grants to You a perpetual," +
                                        " worldwide, non-exclusive, no-charge, royalty-free, irrevocable" +
                                        " (except as stated in this section) patent license to make, have made," +
                                        " use, offer to sell, sell, import, and otherwise transfer the Work," +
                                        " where such license applies only to those patent claims licensable" +
                                        " by such Contributor that are necessarily infringed by their" +
                                        " Contribution(s) alone or by combination of their Contribution(s)" +
                                        " with the Work to which such Contribution(s) was submitted. If You" +
                                        " institute patent litigation against any entity (including a" +
                                        " cross-claim or counterclaim in a lawsuit) alleging that the Work" +
                                        " or a Contribution incorporated within the Work constitutes direct" +
                                        " or contributory patent infringement, then any patent licenses" +
                                        " granted to You under this License for that Work shall terminate" +
                                        " as of the date such litigation is filed.\n" +
                                        "\n" +
                                        " 4. Redistribution. You may reproduce and distribute copies of the" +
                                        " Work or Derivative Works thereof in any medium, with or without" +
                                        " modifications, and in Source or Object form, provided that You" +
                                        " meet the following conditions:\n" +
                                        "\n" +
                                        " (a) You must give any other recipients of the Work or" +
                                        " Derivative Works a copy of this License; and\n" +
                                        "\n" +
                                        " (b) You must cause any modified files to carry prominent notices" +
                                        " stating that You changed the files; and\n" +
                                        "\n" +
                                        " (c) You must retain, in the Source form of any Derivative Works" +
                                        " that You distribute, all copyright, patent, trademark, and" +
                                        " attribution notices from the Source form of the Work," +
                                        " excluding those notices that do not pertain to any part of" +
                                        " the Derivative Works; and\n" +
                                        "\n" +
                                        " (d) If the Work includes a \"NOTICE\" text file as part of its" +
                                        " distribution, then any Derivative Works that You distribute must" +
                                        " include a readable copy of the attribution notices contained" +
                                        " within such NOTICE file, excluding those notices that do not" +
                                        " pertain to any part of the Derivative Works, in at least one" +
                                        " of the following places: within a NOTICE text file distributed" +
                                        " as part of the Derivative Works; within the Source form or" +
                                        " documentation, if provided along with the Derivative Works; or," +
                                        " within a display generated by the Derivative Works, if and" +
                                        " wherever such third-party notices normally appear. The contents" +
                                        " of the NOTICE file are for informational purposes only and" +
                                        " do not modify the License. You may add Your own attribution" +
                                        " notices within Derivative Works that You distribute, alongside" +
                                        " or as an addendum to the NOTICE text from the Work, provided" +
                                        " that such additional attribution notices cannot be construed" +
                                        " as modifying the License.\n" +
                                        "\n" +
                                        " You may add Your own copyright statement to Your modifications and" +
                                        " may provide additional or different license terms and conditions" +
                                        " for use, reproduction, or distribution of Your modifications, or" +
                                        " for any such Derivative Works as a whole, provided Your use," +
                                        " reproduction, and distribution of the Work otherwise complies with" +
                                        " the conditions stated in this License.\n" +
                                        "\n" +
                                        " 5. Submission of Contributions. Unless You explicitly state otherwise," +
                                        " any Contribution intentionally submitted for inclusion in the Work" +
                                        " by You to the Licensor shall be under the terms and conditions of" +
                                        " this License, without any additional terms or conditions." +
                                        " Notwithstanding the above, nothing herein shall supersede or modify" +
                                        " the terms of any separate license agreement you may have executed" +
                                        " with Licensor regarding such Contributions.\n" +
                                        "\n" +
                                        " 6. Trademarks. This License does not grant permission to use the trade" +
                                        " names, trademarks, service marks, or product names of the Licensor," +
                                        " except as required for reasonable and customary use in describing the" +
                                        " origin of the Work and reproducing the content of the NOTICE file.\n" +
                                        "\n" +
                                        " 7. Disclaimer of Warranty. Unless required by applicable law or" +
                                        " agreed to in writing, Licensor provides the Work (and each" +
                                        " Contributor provides its Contributions) on an \"AS IS\" BASIS," +
                                        " WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or" +
                                        " implied, including, without limitation, any warranties or conditions" +
                                        " of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A" +
                                        " PARTICULAR PURPOSE. You are solely responsible for determining the" +
                                        " appropriateness of using or redistributing the Work and assume any" +
                                        " risks associated with Your exercise of permissions under this License.\n" +
                                        "\n" +
                                        " 8. Limitation of Liability. In no event and under no legal theory," +
                                        " whether in tort (including negligence), contract, or otherwise," +
                                        " unless required by applicable law (such as deliberate and grossly" +
                                        " negligent acts) or agreed to in writing, shall any Contributor be" +
                                        " liable to You for damages, including any direct, indirect, special," +
                                        " incidental, or consequential damages of any character arising as a" +
                                        " result of this License or out of the use or inability to use the" +
                                        " Work (including but not limited to damages for loss of goodwill," +
                                        " work stoppage, computer failure or malfunction, or any and all" +
                                        " other commercial damages or losses), even if such Contributor" +
                                        " has been advised of the possibility of such damages.\n" +
                                        "\n" +
                                        " 9. Accepting Warranty or Additional Liability. While redistributing" +
                                        " the Work or Derivative Works thereof, You may choose to offer," +
                                        " and charge a fee for, acceptance of support, warranty, indemnity," +
                                        " or other liability obligations and/or rights consistent with this" +
                                        " License. However, in accepting such obligations, You may act only" +
                                        " on Your own behalf and on Your sole responsibility, not on behalf" +
                                        " of any other Contributor, and only if You agree to indemnify," +
                                        " defend, and hold each Contributor harmless for any liability" +
                                        " incurred by, or claims asserted against, such Contributor by reason" +
                                        " of your accepting any such warranty or additional liability.\n" +
                                        "\n" +
                                        "END OF TERMS AND CONDITIONS"
                                )

                            }
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                FilledTonalButton(onClick = { hideDialog() }) {
                    Text(
                        text = stringResource(id = R.string.dismiss),
                    )
                }
            },
        )
    }
}

@Composable
private fun TitleDetailDialog(
    modifier: Modifier = Modifier,
    title: String
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
