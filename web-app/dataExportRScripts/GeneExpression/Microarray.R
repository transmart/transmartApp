args <- commandArgs(TRUE)
File1 <- args[1]
File2 <- args[2]

Data1 <- read.csv(File1, header = TRUE)
Data2 <- read.csv(File2, header = TRUE)
Mean1 <- rowSums(Data1[,2:ncol(Data1)])/(ncol(Data1)-1)
Mean2 <- rowSums(Data2[,2:ncol(Data2)])/(ncol(Data2)-1)

FC <- 2^Mean2/2^Mean1
Pval = 1
i <- 1
while (i < nrow(Data1)+1) {
	Pval[i] <- t.test(Data1[i, 2:ncol(Data1)], Data2[i, 2:ncol(Data2)])$p.value;
	i <- i+1
}
Qval <- p.adjust(Pval, method="fdr", n=length(Pval))

Data <- cbind(Data1, Data2[2:ncol(Data2)])
Data <- cbind(Data, matrix(FC, length(FC), 1))
colnames(Data)[ncol(Data)] = "FC"
Data <- cbind(Data, matrix(Pval, length(Qval), 1))
colnames(Data)[ncol(Data)] = "p-value"
Data <- cbind(Data, matrix(Qval, length(Qval), 1))
colnames(Data)[ncol(Data)] = "q-value"
Data <- head(Data[order(Data[,"p-value"]),], n = 100L)
Data <- head(Data[order(Data[,"FC"]),], n = 100L)

unlink(File1, recursive = FALSE, force = TRUE)
unlink(File2, recursive = FALSE, force = TRUE)

write.csv(Data, file=File1)

q(save="no")
