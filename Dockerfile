FROM gcc:10
WORKDIR /app/
COPY ./* ./
RUN gcc program.c -o program
RUN chmod +x program