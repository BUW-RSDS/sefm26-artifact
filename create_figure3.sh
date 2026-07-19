#!/bin/bash
pushd paper_results;
../benchexec/contrib/plots/quantile-generator.py gdart.2026-06-20_06-39-59.results.sv-comp20_prop-reachsafety_java.ReachSafety-Java.xml.bz2 > gdart.2026-06-20_06-39-59.quantile.csv
../benchexec/contrib/plots/quantile-generator.py gdart.2026-06-25_17-11-31.results.sv-comp20_prop-reachsafety_java.ReachSafety-Java.xml.bz2 > gdart.2026-06-25_17-11-31.quantile.csv
../benchexec/contrib/plots/quantile-generator.py gdart.2026-06-25_23-29-31.results.sv-comp20_prop-reachsafety_java.ReachSafety-Java.xml.bz2 > gdart.2026-06-25_23-29-31.quantile.csv
../benchexec/contrib/plots/quantile-generator.py gdart.2025-12-09_16-38-04.results.SV-COMP26_valid-assert.Java.valid-assert.Main.xml.bz2 > gdart.sv-comp26.quantile.csv



echo """
\documentclass[boarder=2cm]{standalone}
\usepackage[
    group-digits=integer, group-minimum-digits=4, % group digits by thousands
    free-standing-units, unit-optional-argument, % easier input of numbers with units
    ]{siunitx}[=v2]
\usepackage{pgfplots}
\pgfplotsset{
    compat=1.9,
    log ticks with fixed point, % no scientific notation in plots
    table/col sep=tab, % only tabs are column separators
    unbounded coords=jump, % better have skips in a plot than appear to be interpolating
    filter discard warning=false, % Don't complain about empty cells
    }
\SendSettingsToPgf % use siunitx formatting settings in PGF, too

\begin{document}
\begin{tikzpicture}
\begin{semilogyaxis}[
    % Which column to be taken from each file
    /pgfplots/table/y index=4,
    /pgfplots/table/header=false,
    % axis labels
    xlabel=n-th fastest result,
    ylabel=CPU time (\second),
    % axis ranges
    xmin=0,
    ymin=0.1,
    ymax=2000,
    mark repeat=200,
    % legend
    legend entries={GDart Run 1, GDart Run 2, GDart Run 3, GDart SV-COMP'26},
    every axis legend/.append style={at={(0,1)}, anchor=north west, outer xsep=5pt, outer ysep=5pt,},
    ]
    \addplot+ table {./gdart.2026-06-20_06-39-59.quantile.csv};
    \addplot+ table {./gdart.2026-06-25_17-11-31.quantile.csv};
    \addplot+ table {./gdart.2026-06-25_23-29-31.quantile.csv};
    \addplot+ table {./gdart.sv-comp26.quantile.csv};
\end{semilogyaxis}
\end{tikzpicture}

\begin{tikzpicture}
\begin{semilogyaxis}[
    % Which column to be taken from each file
    /pgfplots/table/y index=5,
    /pgfplots/table/header=false,
    % axis labels
    xlabel=n-th fastest result,
    ylabel=Wall time (\second),
    % axis ranges
    xmin=0,
    ymin=0.1,
    ymax=2000,
    mark repeat=200,
    % legend
    legend entries={GDart Run 1, GDart Run 2, GDart Run 3, GDart SV-COMP'26},
    every axis legend/.append style={at={(0,1)}, anchor=north west, outer xsep=5pt, outer ysep=5pt,},
    ]
    \addplot+ table {./gdart.2026-06-20_06-39-59.quantile.csv};
    \addplot+ table {./gdart.2026-06-25_17-11-31.quantile.csv};
    \addplot+ table {./gdart.2026-06-25_23-29-31.quantile.csv};
    \addplot+ table {./gdart.sv-comp26.quantile.csv};
\end{semilogyaxis}
\end{tikzpicture}
\end{document}
""" > figure3.tex
pdflatex figure3.tex || true
mv figure3.pdf .. || true
popd;
