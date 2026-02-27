FROM node:20-alpine

WORKDIR /app

COPY package*.json ./

#Almost the same as npm install but should be faster. Uses the exact versions from package-lock.json
RUN npm ci

#says copy everything else from the frontend folder into /app so the container gets full source code
COPY . .
RUN npm run
EXPOSE 3000

#Tells docker what to run when the container starts
#npm run dev starts the Next.js development server
#-H 0.0.0.0 tells Docker to listen on all network interfaces (by default Next.js  it only listens to localhost)
#-p 3000 tells Next to use port 3000
CMD ["npm","run","start","--","-H","0.0.0.0","-p","3000"]
